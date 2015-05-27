package program.main;

import core.exceptions.IncorrectHeaderException;
import core.main.*;
import nf.fr.eraasoft.pool.PoolException;
import program.main.database.entities.CardMasterEntity;
import program.main.database.entities.NpcEntity;
import shared.map.CardMaster;
import shared.map.Faction;
import shared.map.Hero;
import shared.other.DataUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author doc
 */
public class MapController {
	private Program program;
	private int updateClientTimeTick = 0;

	public MapController(){
		program = Program.getInstance();
	}

	public void teleportPlayer(ServerCardMaster player, float x, float y){
		byte data[] = new byte[12];
		System.arraycopy(DataUtil.intToByte(player.getId()), 0, data, 0, 4);
		System.arraycopy(DataUtil.floatToByte(x), 0, data, 4, 4);
		System.arraycopy(DataUtil.floatToByte(y), 0, data, 8, 4);

		player.getHero().setX(x);
		player.getHero().setY(y);

		Set<GameClient> whoCanSeeMe = player.getWhoCanSeeMe();
		for(GameClient cl: whoCanSeeMe)
			ReliablePacketManager.sendPacket(Program.getInstance().getLocalServer(), cl.getClient(), Program.HEADER_INSTANT_MOVE, data);
	}

	public void putNpcOnMap(Npc npc){
		program.idNpcMap.put(npc.getCardMaster().getId(), npc);
		program.idCardMasterMap.put(npc.getCardMaster().getId(), npc.getCardMaster());
		program.cardMasterNpcMap.put(npc.getCardMaster(), npc);
		program.clusterGrid.updateCardMaster(npc.getCardMaster());
	}

	public void sendVisibleListToClient(GameClient gc) {
		try {
			Packet packet = Packet.getPool().getObj();
			packet.setData(Program.HEADER_PLAYERS_IN_SIGHT, gc.getPlayersInSightByte());
			program.getLocalServer().send(gc.getClient(), packet);
			Packet.getPool().returnObj(packet);
		} catch (PoolException e) {
			e.printStackTrace();
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	private void sendFactionInfo(Client client) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		Collection<Faction> factions = program.getFactionController().getFactions();

		stream.writeByte(factions.size());

		for (Faction faction: factions){
			stream.write(faction.getId());
			stream.writeUTF(faction.getName());
		}

		ReliablePacketManager.sendPacket(program.localServer, client, Program.HEADER_GET_FACTION_INFO, bytes.toByteArray());
	}

	public void sendWorldInfo(GameClient client){
		try {
			sendFactionInfo(client.getClient());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Npc createNpc(String name){
		try {
			Npc npc = new Npc();

			CardMasterEntity masterEntity = new CardMasterEntity();
			masterEntity.setName(name);
			masterEntity.setData(npc.getCardMaster().getData());

			program.getDatabase().getCardMasterDao().create(masterEntity);

			NpcEntity npcEntity = new NpcEntity();
			npcEntity.setCardMaster(masterEntity);

			program.getDatabase().getNpcDao().create(npcEntity);

			npc.getCardMaster().setId(masterEntity.getId());
			npc.getCardMaster().setName(name);
			npc.createDefaultInventory();
			return npc;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void loadNpcTable(){
		try {
			List<NpcEntity> npcEntities = program.getDatabase().getNpcDao().queryForAll();

			for (NpcEntity entity: npcEntities){
				CardMasterEntity masterEntity = entity.getCardMaster();

				Npc npc = new Npc();

				ServerCardMaster cardMaster = npc.getCardMaster();

				cardMaster.setId(masterEntity.getId());
				cardMaster.setName(masterEntity.getName());
				cardMaster.setData(masterEntity.getData());
				cardMaster.getInventory().loadItems();

				putNpcOnMap(npc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveCardMaster(ServerCardMaster master) throws Exception{
		CardMasterEntity entity = program.getDatabase().getCardMasterDao().queryForId(master.getId());
		entity.setData(master.getData());

		Program.getInstance().getDatabase().getCardMasterDao().update(entity);
		master.getInventory().updateItems();
	}

	public void disconnectPlayer(GameClient client){
		try {
			saveCardMaster(client.getCardMaster());

			// Remove client from whocanseeme
			for(GameClient cl: program.getGameClients())
				cl.getCardMaster().getWhoCanSeeMe().remove(client);
			program.getDuelController().cancel(client.getCardMaster());
			program.getTradingController().cancel(client.getCardMaster());
			program.getGameClients().remove(client);
			program.idCardMasterMap.remove(client.getCardMaster().getId());
			program.idGameClientMap.remove(client.getId());
			program.clusterGrid.removeCardMaster(client.getCardMaster());
			client.getCardMaster().setState(CardMaster.STATE_REMOVED);
			ReliablePacketManager.clientDisconnected(client.getClient());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(){
		List<GameClient> gameClients = program.getGameClients();

		// Update client time every 200 ms
		if (++updateClientTimeTick == 10){
			for (int i = 0; i < gameClients.size(); i++) {
				GameClient gc = gameClients.get(i);
				// Also update client visible player list
				gc.updatePlayersInSight();
				// If visible list has changed, send changes to player immediately
				if (gc.isPlayersInSightChanged())
					sendVisibleListToClient(gc);

				gc.updateTime();
				if (!gc.isStillOnline()){
					disconnectPlayer(gc);
					i--;
				}
			}

			updateClientTimeTick = 0;
		}


		// Update heroes every tick
		// Only update those who are not in battle
		for (GameClient gc: program.getGameClients()){
			if (gc.getCardMaster().getState() == CardMaster.STATE_IN_GLOBAL_MAP){
				gc.getCardMaster().getHero().update();
				//program.getBattleController().checkIfClientIsGoingToBattle(gc);

				if (gc.getCardMaster().getHero().getOrder() != Hero.ORDER_STOP)
					program.clusterGrid.updateCardMaster(gc.getCardMaster());
			}
		}

	}
}
