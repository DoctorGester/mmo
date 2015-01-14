package shared.board;

import shared.board.data.UnitData;
import shared.items.types.CardItem;
import shared.map.CardMaster;

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

	public List<? extends Unit> getUnits();
	public List<? extends CardMaster> getCardMasters();
	public List<? extends CardItem> getPickedCards(CardMaster cardMaster);

	public void addCardMaster(CardMaster cardMaster);
	public void playerFinishedPlacement(CardMaster cardMaster);

	public void addAlliance(Alliance alliance);
	public boolean areAllies(CardMaster... cardMasters);

	public Cell getCell(int x, int y);
	public Cell getCellChecked(int x, int y);

	public void setPlacementArea(Rectangle area[]);
	public void setTimeRemaining(float time);
	public void setTurnTime(float time);
	public void selectTurningPlayer();

	public Alliance getAllianceById(int id);
	public Rectangle[] getPlacementArea();

	public void addUnit(Unit unit);
	public void removeUnit(Unit unit);
	public void skipTurn();
	public void nextTurn();

	public Buff addBuff(String id, int timesToRepeat, int period, int initialDelay, Object data);
	public Buff addBuff(String id, int timesToRepeat, int period, Object data);
	public Buff addBuff(String id, int timesToRepeat, int period);

	public void update(float tpf);
}
