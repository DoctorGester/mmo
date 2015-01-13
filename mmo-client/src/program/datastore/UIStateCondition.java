package program.datastore;

import program.main.Program;

/**
 * @author doc
 */
public class UIStateCondition implements Condition{
	private String state;

	public UIStateCondition(String state) {
		this.state = state;
	}

	@Override
	public boolean check() {
		return Program.getInstance().getMainFrame().hasUIState(state);
	}
}
