package core.board.interfaces;

/**
 * @author doc
 */
public interface Buff {

	public void update();
	public void end();

	public boolean hasEnded();

	public Object getData();
}
