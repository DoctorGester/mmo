package core.main;

import core.board.ServerBoard;
import program.main.FactionController;
import program.main.Program;
import shared.board.Board;
import shared.board.Unit;
import shared.map.CardMaster;
import shared.map.Faction;
import shared.map.Hero;
import shared.map.Stat;
import shared.other.DataUtil;
import shared.other.Vector2;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCardMaster implements CardMaster {

	private int state = STATE_IN_GLOBAL_MAP;

	private String name;
	private ServerBoard currentBoard;
	private ServerHero hero;
	private Unit usedUnit;

    private int id;
	private int battleId;
	private int type;

	private Map<Faction, Integer> reputation;
	private Map<Stat, Integer> stats;

	private Set<GameClient> whoCanSeeMe;

	private ServerInventory inventory = new ServerInventory();
	
	public ServerCardMaster(){
		hero = new ServerHero(this);

        reputation = new ConcurrentHashMap<Faction, Integer>();
		stats = new ConcurrentHashMap<Stat, Integer>();
		whoCanSeeMe = Collections.synchronizedSet(new HashSet<GameClient>());

		inventory.setOwner(this);

		for (Stat stat: Stat.values())
			stats.put(stat, 0);
	}

	public Set<GameClient> getWhoCanSeeMe() {
		return whoCanSeeMe;
	}

	@Override
	public ServerInventory getInventory() {
		return inventory;
	}

	@Override
	public void setStat(Stat stat, int value){
		stats.put(stat, value);
	}

	@Override
	public int getStat(Stat stat){
		return stats.get(stat);
	}

    @Override
	public void setReputation(Faction faction, int reputation){
        this.reputation.put(faction, reputation);
    }

    @Override
	public int getReputation(Faction faction){
        if (reputation.containsKey(faction))
            return reputation.get(faction);
        return 0;
    }

    @Override
	public int getId() {
        return id;
    }

    @Override
	public void setId(int id) {
        this.id = id;
    }

	@Override
	public int getBattleId() {
		return battleId;
	}

	@Override
	public void setBattleId(int battleId) {
		this.battleId = battleId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Unit getUsedUnit() {
		return usedUnit;
	}
	
	@Override
	public void setUsedUnit(Unit usedUnit) {
		this.usedUnit = usedUnit;
	}
	
	@Override
	public ServerHero getHero() {
		return hero;
	}
	
	@Override
	public ServerBoard getCurrentBoard() {
		return currentBoard;
	}
	
	@Override
	public void setCurrentBoard(Board currentBoard) {
		this.currentBoard = (ServerBoard) currentBoard;
	}
	
	@Override
	public int getState() {
		return state;
	}
	
	@Override
	public void setState(int state){
		this.state = state;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	public void setData(byte data[]){
		try {
			Program program = Program.getInstance();

			DataInputStream stream = DataUtil.stream(data);

			hero.setX(stream.readFloat());
			hero.setY(stream.readFloat());

			int factionAmount = stream.readByte();

			for(int i = 0; i < factionAmount; i++){
				Faction faction = program.getFactionController().getFactionById(stream.readByte());

				setReputation(faction, stream.readByte());
			}

			for (Stat stat: Stat.values())
				setStat(stat, stream.readByte());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] getData(){
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			FactionController factionController = Program.getInstance().getFactionController();
			Collection<Faction> factions = factionController.getFactions();

			stream.write(DataUtil.floatToByte(hero.getX()));
			stream.write(DataUtil.floatToByte(hero.getY()));

			stream.write(factions.size());

			for (Faction faction: factions){
				stream.write(faction.getId());
				stream.write(getReputation(faction));
			}

			for(Stat stat: Stat.values())
				stream.write(getStat(stat));

			return stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getPositionInfo() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		stream.writeFloat(hero.getX());
		stream.writeFloat(hero.getY());
		stream.writeByte(hero.getOrder());

		byte order = (byte) hero.getOrder();

		switch(order){
			case Hero.ORDER_MOVE:{
				int pathTarget = hero.getCurrentPathTarget();
				Vector2[] path = hero.getPath();

				stream.writeByte(path.length - pathTarget);

				for (int i = pathTarget; i < path.length; i++){
					stream.writeFloat(path[i].x);
					stream.writeFloat(path[i].y);
				}

				break;
			}
		}

		return bytes.toByteArray();
	}

	public byte[] getCharacterInfo() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		stream.write(getType());
		stream.writeUTF(getName());

		return bytes.toByteArray();
	}
}
