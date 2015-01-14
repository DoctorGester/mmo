package core.main;

import shared.map.Hero;
import shared.other.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class HeroPath {
	// The bigger this factor is, more points will be put into the output
	private static final float OPTIMIZE_FACTOR = 0.9f;
	
	private PathingMap map;
	
	private int startx, starty, endx, endy;
	private int heroSize, maxLength;
	private boolean found;
	
	private Vector2 finalPath[];
	
	private Hero hero;
	
	public HeroPath(Hero hero, int heroSize, float orderx, float ordery, PathingMap map, int maxLength){
		this.hero = hero;
		this.map = map;
		this.heroSize = heroSize;
		this.maxLength = maxLength;

		float halfSize = PathingMap.CELL_SIZE / 2f;
		
		startx = (int) ((hero.getX() + halfSize) / PathingMap.CELL_SIZE);
		starty = (int) ((hero.getY() + halfSize) / PathingMap.CELL_SIZE);
		
		endx = (int) (orderx / PathingMap.CELL_SIZE);
		endy = (int) (ordery / PathingMap.CELL_SIZE);
	}
	
	private int heuristic(int sx, int sy, int ex, int ey){
		return Math.abs(ex - sx) + Math.abs(ey - sy);
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
		private int x, y;
		private PathCell parent;
		private int gcost, fcost, cost;
		
		public PathCell(int x, int y, PathCell parent, int gcost, int fcost, int cost){
			this.x = x;
			this.y = y;
			this.parent = parent;
			this.gcost = gcost;
			this.fcost = fcost;
			this.cost = cost;
		}
	}
	
	private PathCell checkNeighbour(PathCell current, int dx, int dy, int cost){
		int x = current.x + dx,
			y = current.y + dy;
		
		if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight())
			return null;

		// Checking square around hero
		for(int sy = 0; sy < heroSize; sy++)
			for(int sx = 0; sx < heroSize; sx++)
				if (!map.isPointPathable(x + sx, y + sy))
					return null;

		return new PathCell(x, y, current, 0, 0, cost);
	}
	
	private boolean inOpenList(PathCell p){
		for(int i = 0; i < openListSize; i++)
			if (openList[i].x == p.x && openList[i].y == p.y)
				return true;
		return false;
	}
	
	private boolean inClosedList(PathCell p){
		for(int i = 0; i < closedListSize; i++)
			if (closedList[i].x == p.x && closedList[i].y == p.y)
				return true;
		return false;
	}
	
	public Vector2[] getPath(){
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
	
	private Vector2[] optimizePath(Vector2[] path){
		if (path.length <= 2)
			return path;
		
		// Array length is definitely >= 3
		int optSize = 1;
		Vector2 opt[] = new Vector2[path.length];
		opt[0] = path[0];
		
		Vector2 lastDir = path[1].clone().subLocal(path[0]).normalizeLocal();
		for(int i = 2; i < path.length; i++){
			Vector2 thisDir = path[i].clone().subLocal(path[i - 1]).normalizeLocal();
			
			// If direction is different enough, addLocal current vector to array,
			// save this direction
			if (thisDir.dot(lastDir) < OPTIMIZE_FACTOR){
				lastDir = thisDir;
				opt[optSize++] = path[i - 1];
			}
		}
		opt[optSize++] = path[path.length - 1];
		
		opt = Arrays.copyOf(opt, optSize);
		return opt;
	}
	
	public boolean find(){
		found = false;
		if (!map.contains(endx, endy) || !map.isPointPathable(endx, endy))
			return false;
		
		openList = new PathCell[(int) Math.pow(maxLength * 2, 2)];
		closedList = new PathCell[openList.length];
		
		PathCell start = new PathCell(startx, starty, null, 0, heuristic(startx, starty, endx, endy), 0),
				 end = new PathCell(endx, endy, null, 0, 0, 0);
		
		// Temp array
		PathCell neighbours[] = new PathCell[8];
		
		addOpenList(start);
		while(openListSize != 0){

			int currentIndex = 0;
			PathCell current = openList[0];
			
			// Checkin every cell in the openList to find cell with lowest fcost value
			for(int i = 1; i < openListSize; i++){
				if (openList[i].fcost < current.fcost){
					current = openList[i];
					currentIndex = i;
				}
			}
			
			// Exit if we have found our path successfully
			if (current.x == end.x && current.y == end.y){
				
				// Fill path array
				ArrayList<Vector2> path = new ArrayList<Vector2>();
				for(; current != null; current = current.parent)
					path.add(new Vector2(current.x * PathingMap.CELL_SIZE, current.y * PathingMap.CELL_SIZE));
				
				Collections.reverse(path);
				path.set(0, new Vector2(hero.getX(), hero.getY()));
				
				finalPath = optimizePath(path.toArray(new Vector2[path.size()]));
				
				found = true;
				
				return true;
			}
			
			// Removing current from open list
			removeOpenList(currentIndex);
			addClosedList(current);
			
			// We don't need to check neighbours if maxLength is reached
			if (current.gcost + 1 >= maxLength * 10)
				continue;
			
			// Checking adjacent cells
			neighbours[0] = checkNeighbour(current, 1, 0, 10);
			neighbours[1] = checkNeighbour(current, -1, 0, 10);
			neighbours[2] = checkNeighbour(current, 0, 1, 10);
			neighbours[3] = checkNeighbour(current, 0, -1, 10);
			
			if (neighbours[1] != null && neighbours[3] != null)
				neighbours[4] = checkNeighbour(current, -1, -1, 14);
			
			if (neighbours[0] != null && neighbours[3] != null)
				neighbours[5] = checkNeighbour(current, 1, -1, 14);
			
			if (neighbours[0] != null && neighbours[2] != null)
				neighbours[6] = checkNeighbour(current, 1, 1, 14);
			
			if (neighbours[1] != null && neighbours[2] != null)
				neighbours[7] = checkNeighbour(current, -1, 1, 14);
			
			// Go through all nonnull neighbours
			for(int i = 0; i < 7; i++){
				if (neighbours[i] == null)
					continue;
				
				PathCell n = neighbours[i];
				int tentative = current.gcost + n.cost;
				
				if (tentative >= n.gcost && inClosedList(n)){
					continue;
				}
				
				boolean inOpen = inOpenList(n);
				
				if (!inOpen || tentative < n.gcost){
					n.parent = current;
					n.gcost = tentative;
					n.fcost = n.gcost + heuristic(n.x, n.y, end.x, end.y);
					if (!inOpen)
						addOpenList(n);
				}
				neighbours[i] = null;
			}
		}
		return false;
	}
}
