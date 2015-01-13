package core.main;


public class PathingMap {
	public static final int CELL_SIZE = 16;

	private int width, height;
	
	private boolean data[];
	
	public PathingMap(byte map[], int width, int height){
		if (map.length != width * height / 8 || map.length % 8 != 0)
			throw new IllegalArgumentException("Data size invalid");
		this.width = width;
		this.height = height;
		
		data = DataUtil.byteToBool(map);
	}
	
	public boolean isPointPathable(int x, int y){
		return !data[y * width + x];
	}
	
	public boolean isRealPointPathable(float fx, float fy){
		int x = (int) (fx / CELL_SIZE),
			y = (int) (fy / CELL_SIZE);
		return isPointPathable(x, y);
	}

	public boolean contains(int x, int y){
		return x >= 0 && y >= 0 && x < width && y < height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
