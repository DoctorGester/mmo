package shared.board;

/**
 * @author doc
 */
public interface Cell {
	public static final int CELL_WIDTH = 36,
							CELL_HEIGHT = 36;

	public static final int CONTENTS_EMPTY = 0x00,
							CONTENTS_UNIT = 0x01,
							CONTENTS_DOODAD = 0x02;

	public int getX();
	public int getY();
	public int getContentsType();
	public Unit getUnit();
	public Board getBoard();

	public void setContentsType(int type);
	public void setUnit(Unit unit);
}
