package shared.board;

import shared.board.data.BuffData;

/**
 * @author doc
 */
public interface Buff {
	public BuffData getBuffData();

	public int getTimesToRepeat();
	public int getPeriod();

	public void update();
	public void end();

	public boolean hasEnded();

	public Object getData();
}
