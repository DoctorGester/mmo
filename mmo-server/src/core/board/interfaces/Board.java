package core.board.interfaces;

import core.board.Alliance;
import core.board.UnitData;
import core.main.CardMaster;
import core.main.inventory.items.CardItem;

import java.awt.*;
import java.util.List;

/**
 * @author doc
 */
public interface Board {
	public static final int STATE_WAIT_FOR_ORDER = 0x00,
							STATE_WAIT_FOR_PICK = 0x04,
							STATE_WAIT_FOR_PLACEMENT = 0x05,
							STATE_GAME_IS_OVER = 0x06;

	public static final int GAME_OVER_WIN = 0x00,
							GAME_OVER_DRAW = 0x01;

	public static final int OWNED_UNITS = 3;

	public int getId();
	public void setId(int id);

	public int getWidth();
	public int getHeight();
	public int getState();
	public short getTurnNumber();
	public CardMaster getCurrentTurningPlayer();

	public List<Unit> getUnits();
	public List<CardMaster> getCardMasters();
	public List<CardItem> getPickedCards(CardMaster cardMaster);

	public void addCardMaster(CardMaster cardMaster);
	public void playerFinishedPlacement(CardMaster cardMaster);

	public void addAlliance(Alliance alliance);
	public boolean areAllies(CardMaster ... cardMasters);

	public Cell getCell(int x, int y);
	public Cell getCellChecked(int x, int y);

	public void setPlacementArea(Rectangle area[]);
	public void setTimeRemaining(float time);
	public void setTurnTime(float time);
	public void selectTurningPlayer();

	public void addUnit(Unit unit);
	public void removeUnit(Unit unit);
	public void unitDies(Unit unit);
	public void checkGameOver();
	public void skipTurn();
	public void nextTurn();

	public Unit handlePickOrder(CardMaster cardMaster, CardItem card, UnitData unitData);
	public boolean handlePlacementOrder(CardMaster owner, Cell selected, Cell order);
	public boolean handleCastOrder(CardMaster owner, Cell selected, Cell target, int spell);
	public boolean handleSimpleOrder(CardMaster owner, Cell selected, Cell order);
	public boolean handleCastCardSpellOrder(CardMaster caster, int cardId);

	public Buff addBuff(String id, int timesToRepeat, int period, int initialDelay, Object data);
	public Buff addBuff(String id, int timesToRepeat, int period, Object data);
	public Buff addBuff(String id, int timesToRepeat, int period);

	public void update(float tpf);
}
