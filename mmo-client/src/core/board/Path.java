package core.board;


public class Path {
	
	private Board board;
	private Cell start, end;
	private int maxLength;
	private boolean found;
	
	private Cell finalPath[];
	
	public Path(Cell start, Cell end, int maxLength){
		if (start.getBoard() != end.getBoard())
			throw new IllegalArgumentException("These cells are from different boards!");
		this.start = start;
		this.end = end;
		this.maxLength = maxLength;
		board = start.getBoard();
	}
	
	private int heuristic(Cell from, Cell to){
		return Math.abs(to.getX() - from.getX()) + Math.abs(to.getY() - from.getY());
	}
	
	private PathCell[] openList;
	private PathCell[] closedList;
	
	private int openListSize = 0;
	private int closedListSize = 0;
	
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
		private Cell cell;
		private PathCell parent;
		private int gcost, fcost;
		
		public PathCell(Cell cell, PathCell parent, int gcost, int fcost){
			this.cell = cell;
			this.parent = parent;
			this.gcost = gcost;
			this.fcost = fcost;
		}
	}
	
	private PathCell checkNeighbour(PathCell current, int dx, int dy){
		int x = current.cell.getX() + dx,
			y = current.cell.getY() + dy;
		
		if (x < 0 || y < 0 || x >= board.getWidth() || y >= board.getHeight())
			return null;
					
		Cell c = board.getCell(x, y);
		
		if (c.getContentsType() != Cell.CONTENTS_EMPTY)
			return null;
		
		for(int i = 0; i < closedListSize; i++)
			if (closedList[i].cell == c)
				return null;
		
		return new PathCell(c, current, 0, 0);
	}
	
	private boolean inOpenList(PathCell p){
		for(int i = 0; i < openListSize; i++)
			if (openList[i].cell == p.cell)
				return true;
		return false;
	}
	
	public Cell[] getPath(){
		if (finalPath == null)
			throw new IllegalStateException("Path not found!");
		return finalPath;
	}
	
	public int getLength(){
		if (finalPath == null)
			throw new IllegalStateException("Path not found!");
		return finalPath.length;
	}
	
	public boolean isFound() {
		return found;
	}
	
	public boolean find(){
		found = false;
		if (heuristic(start, end) > maxLength)
			return false;
		
		openList = new PathCell[(int) Math.pow(maxLength * 2, 2)];
		closedList = new PathCell[openList.length];
		
		PathCell start = new PathCell(this.start, null, 0, heuristic(this.start, this.end)),
				 end = new PathCell(this.end, null, 0, 0);
		
		// Temp array
		PathCell neighbours[] = new PathCell[4];
		
		addOpenList(start);
		while(openListSize != 0){
			
			int currentIndex = 0;
			PathCell current = openList[0];
			
			// Checking every cell in the openList to find cell with lowest fcost value
			for(int i = 0; i < openListSize; i++){
				if (current.fcost > openList[i].fcost){
					current = openList[i];
					currentIndex = i;
				}
			}
			
			// Exit if we have found our path successfully
			if (current.cell == end.cell){
				
				// Fill path array
				finalPath = new Cell[current.gcost + 1];
				for(int i = finalPath.length - 1; i >= 0; i--, current = current.parent)
					finalPath[i] = current.cell;
				
				found = true;
				
				return true;
			}
			
			// Removing current from open list
			removeOpenList(currentIndex);
			addClosedList(current);
			
			// We don't need to check neighbors if maxLength is reached
			if (current.gcost + 1 == maxLength)
				continue;
			
			// Checking adjacent cells
			neighbours[0] = checkNeighbour(current, 1, 0);
			neighbours[1] = checkNeighbour(current, -1, 0);
			neighbours[2] = checkNeighbour(current, 0, 1);
			neighbours[3] = checkNeighbour(current, 0, -1);
			
			// Go through all nonnull neighbors
			for(int i = 0; i < 4; i++){
				if (neighbours[i] == null)
					continue;
				
				PathCell n = neighbours[i];
				int gcost = current.gcost + 1;
				boolean inOpen = inOpenList(n);
				
				if (gcost < n.gcost || !inOpen){
					n.parent = current;
					n.gcost = gcost;
					n.fcost = heuristic(n.cell, end.cell) + gcost;
					if (!inOpen)
						addOpenList(n);
				}
			}
		}
		return false;
	}
}
