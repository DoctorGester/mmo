package core.ui;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import tonegod.gui.core.Screen;

/**
 * @author doc
 */
public class EmptyUIState extends AbstractAppState {
	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	public EmptyUIState(MainFrame frame) {
		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

	}


	public void cleanup() {
		super.cleanup();
	}
}