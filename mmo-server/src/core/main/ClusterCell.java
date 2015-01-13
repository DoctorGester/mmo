package core.main;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ClusterCell {
	public static final float CELL_SIZE = 1024f,
							  HALF_SIZE = CELL_SIZE / 2;

	private Set<CardMaster> cardMastersInside;

	private int x, y;

	public ClusterCell(int x, int y) {
		this.x = x;
		this.y = y;

		cardMastersInside = new CopyOnWriteArraySet<CardMaster>();
	}

	public Set<CardMaster> getCardMastersInside(){
		return cardMastersInside;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String toString(){
		return super.toString() + " [" + x + " : " + y + "]";
	}
}
