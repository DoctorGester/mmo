package core.handlers;

import core.main.*;
import program.main.database.Database;
import program.main.Program;
import program.main.ReliablePacketManager;
import program.main.database.entities.CardMasterEntity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;

public class GetProfileInfoMessageHandler extends PacketHandler{
	private Program program;

	public GetProfileInfoMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	private byte[] formPlayerData(CardMaster master) throws SQLException, IOException {
		Collection<Faction> factions = program.getFactionController().getFactions();

		String name = master.getName();
		int id = master.getId();

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);

		out.writeInt(id);
		out.writeUTF(name);
		out.write(factions.size());

		for (Faction faction: factions){
			out.write(faction.getId());
			out.write(master.getReputation(faction));
		}

		for(Stat stat: Stat.values())
			out.write(master.getStat(stat));

		return bytes.toByteArray();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		try {
			// Exit if packet contains other data than id
			if (data.getData().length != 4)
				return;

			int id = DataUtil.byteToInt(data.getData());

			CardMaster cardMaster = program.getCardMasterById(id);

			if (cardMaster != null){
				ReliablePacketManager.sendPacket(localServer, client, Program.HEADER_GET_PROFILE_INFO, formPlayerData(cardMaster));
				return;
			}

			// Exit if there is no access to database
			Database db = program.getDatabase();

			CardMasterEntity entity = db.getCardMasterDao().queryForId(id);

			if (entity == null)
				return;

			CardMaster stub = new CardMaster();
			stub.setId(entity.getId());
			stub.setData(entity.getData());
			stub.setName(entity.getName());

			ReliablePacketManager.sendPacket(localServer, client, Program.HEADER_GET_PROFILE_INFO, formPlayerData(stub));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
