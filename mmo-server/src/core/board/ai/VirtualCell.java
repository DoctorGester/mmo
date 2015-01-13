package core.board.ai;

import core.board.interfaces.Cell;
import core.board.interfaces.Doodad;
import core.board.interfaces.Unit;

/**
 * @author doc
 */
public class VirtualCell implements Cell {
	protected float weight;

	private int contentsType = Cell.CONTENTS_EMPTY;
	private VirtualBoard board;
	private int x, y;

	private Unit contentUnit = null;
	private Doodad contentDoodad = null;

	public VirtualCell(VirtualBoard board, int x, int y){
		this.board = board;
		this.x = x;
		this.y = y;
	}

	public VirtualBoard getBoard() {
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
		return contentUnit;
	}

	public Doodad getDoodad(){
		return contentDoodad;
	}

	public void setContentsType(int contentsType) {
		this.contentsType = contentsType;
	}

	public void setUnit(Unit u){
		contentUnit = u;
		setContentsType(Cell.CONTENTS_UNIT);
	}

	public void setDoodad(Doodad d){
		contentDoodad = d;
		setContentsType(Cell.CONTENTS_DOODAD);
	}

	public boolean is(Object object){
		if (object instanceof Cell){
			Cell to = (Cell) object;
			return to.getX() == getX() && to.getY() == getY();
		}
		return false;
	}
}
