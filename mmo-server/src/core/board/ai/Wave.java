package core.board.ai;

import shared.board.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Wave {
	
	private VirtualBoard board;
	private VirtualCell start;
	private int maxLength;
	
	private Map<VirtualCell, Integer> pathingMap;
	private List<VirtualCell> available;
	
	public Wave(VirtualBoard board, VirtualCell start, int maxLength){
		this.start = start;
		this.maxLength = maxLength;
		this.board = board;
	}
	
	private PathCell[] openList;
	private PathCell[] closedList;
	
	private int openListSize = 0;
	private int closedListSize = 0;

	public List<VirtualCell> allAvailableCells(){
		return available;
	}

	private void addOpenList(PathCell c){
		openList[openListSize++] = c;
	}
	
	private void removeOpenList(int index){
		openList[index] = openList[--openListSize];
	}
	
	private void addClosedList(PathCell c){
		closedList[closedListSize++] = c;
	}
	
	private static class PathCell{
		private VirtualCell cell;
		private int distance;
		
		public PathCell(VirtualCell cell, int distance){
			this.cell = cell;
			this.distance = distance;
		}
	}
	
	private void checkNeighbour(PathCell current, int dx, int dy){
		int x = current.cell.getX() + dx,
			y = current.cell.getY() + dy;
		
		if (x < 0 || y < 0 || x >= board.getWidth() || y >= board.getHeight())
			return;

		VirtualCell c = board.getCell(x, y);
		
		if (c.getContentsType() != Cell.CONTENTS_EMPTY)
			return;

		for(int i = 0; i < closedListSize; i++)
			if (closedList[i].cell == c)
				return;

		for(int i = 0; i < openListSize; i++)
			if (openList[i].cell == c)
				return;

		PathCell n = new PathCell(c, 0);
		n.distance = current.distance + 1;
		addOpenList(n);
	}
	
	public int getPathing(VirtualCell cell){
		if (pathingMap == null)
			throw new IllegalStateException("Pathing map is not yet calculated!");
		if (!pathingMap.containsKey(cell))
			return -1;
		return pathingMap.get(cell);
	}
	
	public void calculate(){
		pathingMap = new ConcurrentHashMap<VirtualCell, Integer>();
		available = new ArrayList<VirtualCell>();
		
		openList = new PathCell[(int) Math.pow(maxLength * 2, 2)];
		closedList = new PathCell[openList.length];
		
		PathCell start = new PathCell(this.start, 0);
		
		addOpenList(start);
		while(openListSize != 0){
			for(int i = 0; i < openListSize; i++){
				PathCell current = openList[i];
				pathingMap.put(current.cell, current.distance);
				if (current.distance <= maxLength)
					available.add(current.cell);
				
				// Removing current from open list
				removeOpenList(i);
				addClosedList(current);
				i--;
				
				// We don't need to check neighbours if maxLength is reached
				if (current.distance + 1 == maxLength)
					continue;
				
				// Checking adjacent cells
				checkNeighbour(current, 1, 0);
				checkNeighbour(current, -1, 0);
				checkNeighbour(current, 0, 1);
				checkNeighbour(current, 0, -1);
			}
		}
	}
}
