package core.main;

import core.board.interfaces.Board;
import core.board.interfaces.Unit;
import core.main.inventory.Inventory;
import program.main.FactionController;
import program.main.Program;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CardMaster {
	public static final int TYPE_PLAYER = 0x00,
							TYPE_NPC = 0x01;

	public static final int STATE_IN_GLOBAL_MAP = 0x00,
							STATE_IN_BATTLE = 0x01,
							STATE_REMOVED = 0x02;

	private int state = STATE_IN_GLOBAL_MAP;

	private String name;
	private Board currentBoard;
	private Hero hero;
	private Unit usedUnit;

    private int id;
	private int battleId;
	private int type;

	private Map<Faction, Integer> reputation;
	private Map<Stat, Integer> stats;

	private Set<GameClient> whoCanSeeMe;

	private Inventory inventory = new Inventory();
	
	public CardMaster(){
		hero = new Hero(this);

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

	public Inventory getInventory() {
		return inventory;
	}

	public void setStat(Stat stat, int value){
		stats.put(stat, value);
	}

	public int getStat(Stat stat){
		return stats.get(stat);
	}

    public void setReputation(Faction faction, int reputation){
        this.reputation.put(faction, reputation);
    }

    public int getReputation(Faction faction){
        if (reputation.containsKey(faction))
            return reputation.get(faction);
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public int getBattleId() {
		return battleId;
	}

	public void setBattleId(int battleId) {
		this.battleId = battleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Unit getUsedUnit() {
		return usedUnit;
	}
	
	public void setUsedUnit(Unit usedUnit) {
		this.usedUnit = usedUnit;
	}
	
	public Hero getHero() {
		return hero;
	}
	
	public void setHero(Hero hero) {
		this.hero = hero;
	}
	
	public Board getCurrentBoard() {
		return currentBoard;
	}
	
	public void setCurrentBoard(Board currentBoard) {
		this.currentBoard = currentBoard;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state){
		this.state = state;
	}

	public int getType() {
		return type;
	}

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
				Vector2f[] path = hero.getPath();

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
