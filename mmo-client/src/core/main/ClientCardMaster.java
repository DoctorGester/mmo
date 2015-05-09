package core.main;

import shared.board.Board;
import shared.board.Unit;
import shared.items.Inventory;
import shared.map.*;
import shared.map.Faction;
import shared.other.Vector2;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCardMaster implements CardMaster {
	public static final int TYPE_PLAYER = 0x00,
							TYPE_NPC = 0x01;

	public static final int STATE_IN_GLOBAL_MAP = 0x00,
							STATE_IN_BATTLE = 0x01;

	private int state = STATE_IN_GLOBAL_MAP;

	private String name = "Undefined";
	private Board currentBoard;
	private ClientHero hero;
	private Unit usedUnit;

    private int id;
	private int battleId;
	private int type;

	private Map<Faction, Integer> reputation;
	private Map<Stat, Integer> stats;

	private boolean initialized = false;

	public ClientCardMaster(){
		hero = new ClientHero();

		reputation = new ConcurrentHashMap<Faction, Integer>();
		stats = new ConcurrentHashMap<Stat, Integer>();

		for (Stat stat: Stat.values())
			stats.put(stat, 0);
	}

	@Override
	public Inventory getInventory() {
		throw new UnsupportedOperationException();
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

	public ClientHero getHero() {
		return hero;
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

	public void setPositionInfo(DataInputStream stream) throws IOException{
		hero.setX(stream.readFloat());
		hero.setY(stream.readFloat());

		hero.setOrder(stream.readByte());

		switch (hero.getOrder()){
			case Hero.ORDER_MOVE:{
				int length = stream.readByte();
				Vector2[] path = new Vector2[length];

				for (int i = 0; i < length; i++){
					path[i] = new Vector2(stream.readFloat(), stream.readFloat());
				}

				hero.setPath(path);

				break;
			}
		}
	}

	public void setCharacterInfo(DataInputStream stream) throws IOException{
		setType(stream.readByte());
		setName(stream.readUTF());
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CardMaster that = (CardMaster) o;

		return getId() == that.getId();
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "CardMaster{id = " + id + "}";
	}
}
