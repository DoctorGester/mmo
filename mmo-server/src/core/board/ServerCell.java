package core.board;

import shared.board.Board;
import shared.board.Cell;
import shared.board.Doodad;
import shared.board.Unit;

public class ServerCell implements Cell {
	private int contentsType = CONTENTS_EMPTY;
	private Board board;
	private int x, y;
	
	private Unit contentUnit = null;
	private Doodad contentDoodad = null;
	
	public ServerCell(Board board, int x, int y){
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
