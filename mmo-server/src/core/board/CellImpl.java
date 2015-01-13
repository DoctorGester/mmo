package core.board;

import core.board.interfaces.Board;
import core.board.interfaces.Cell;
import core.board.interfaces.Doodad;
import core.board.interfaces.Unit;

public class CellImpl implements Cell {
	private int contentsType = CONTENTS_EMPTY;
	private Board board;
	private int x, y;
	
	private Unit contentUnit = null;
	private Doodad contentDoodad = null;
	
	public CellImpl(Board board, int x, int y){
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
}
