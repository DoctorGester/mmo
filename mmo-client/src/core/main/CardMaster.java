package core.main;

import com.jme3.math.Vector2f;
import core.board.Board;
import core.board.Unit;
import program.main.Program;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CardMaster {
	public static final int TYPE_PLAYER = 0x00,
							TYPE_NPC = 0x01;

	public static final int STATE_IN_GLOBAL_MAP = 0x00,
							STATE_IN_BATTLE = 0x01;

	private int state = STATE_IN_GLOBAL_MAP;

	private String name = "Undefined";
	private Board currentBoard;
	private Hero hero;
	private Unit usedUnit;

    private int id;
	private int battleId;
	private int type;

	private Map<Stat, Integer> stats;

	private boolean initialized = false;

	public CardMaster(){
		hero = new Hero();

		stats = new ConcurrentHashMap<Stat, Integer>();

		for (Stat stat: Stat.values())
			stats.put(stat, 0);
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
				Vector2f[] path = new Vector2f[length];

				for (int i = 0; i < length; i++){
					path[i] = new Vector2f(stream.readFloat(), stream.readFloat());
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

		return id == that.id;
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
