package program.main;

import core.board.interfaces.Cell;

public class Util {
	public static int distance(Cell from, Cell to){
		return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
	}
}
