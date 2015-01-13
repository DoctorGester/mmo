package program.datastore;

import program.main.Program;

/**
 * @author doc
 */
public class GameStateCondition implements Condition{
	private int state;

	public GameStateCondition(int state) {
		this.state = state;
	}

	@Override
	public boolean check() {
		return Program.getInstance().getClientState() == state;
	}
}
