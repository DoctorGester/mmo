package shared.map;

import shared.board.Board;
import shared.board.Unit;
import shared.items.Inventory;

import java.io.IOException;

public interface CardMaster {
	int TYPE_PLAYER = 0x00;
	int TYPE_NPC = 0x01;

	int STATE_IN_GLOBAL_MAP = 0x00;
	int STATE_IN_BATTLE = 0x01;
	int STATE_REMOVED = 0x02;

	Inventory getInventory();

	void setStat(Stat stat, int value);

	int getStat(Stat stat);

	void setReputation(Faction faction, int reputation);

	int getReputation(Faction faction);

	int getId();

	void setId(int id);

	int getBattleId();

	void setBattleId(int battleId);

	String getName();

	void setName(String name);

	Unit getUsedUnit();

	void setUsedUnit(Unit usedUnit);

	Hero getHero();

	Board getCurrentBoard();

	void setCurrentBoard(Board currentBoard);

	int getState();

	void setState(int state);

	int getType();

	void setType(int type);
}
