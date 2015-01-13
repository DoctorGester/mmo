package core.board;

public class Cell {
	public static final int CELL_WIDTH = 36,
							CELL_HEIGHT = 36;

	public static final int CONTENTS_EMPTY = 0x00,
							CONTENTS_UNIT = 0x01,
							CONTENTS_DOODAD = 0x02;

	private int contentsType = CONTENTS_EMPTY;
	private Board board;
	private int x, y;
	
	private Unit contentUnit = null;
	private Doodad contentDoodad = null;
	
	public Cell(Board board, int x, int y){
		this.board = board;
		this.x = x;
		this.y = y;
	}
	
	public Board getBoard() {
		return board;
	}
	
	public int getContentsType() {
		return contentsType;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Unit getUnit(){
		if (getContentsType() != CONTENTS_UNIT)
			throw new IllegalStateException("ContentsType is wrong!");
		return contentUnit;
	}

	public Doodad getDoodad(){
		if (getContentsType() != CONTENTS_DOODAD)
			throw new IllegalStateException("ContentsType is wrong!");
		return contentDoodad;
	}
	
	public void setContentsType(int contentsType) {
		this.contentsType = contentsType;
	}
	
	public void setUnit(Unit u){
		contentUnit = u;
		setContentsType(CONTENTS_UNIT);
	}
	
	public void setDoodad(Doodad d){
		contentDoodad = d;
		setContentsType(CONTENTS_DOODAD);
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "Cell{" +
				"y=" + y +
				", x=" + x +
				'}';
	}
}
