package core.ui;

import com.jme3.app.state.AbstractAppState;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import program.main.Program;
import tonegod.gui.core.Screen;

public abstract class AbstractUIState extends AbstractAppState {
	protected final Program program;
	protected final MainFrame frame;
	protected final Screen screen;
	protected final Vector2f dimension;

	public AbstractUIState(){
		program = Program.getInstance();
		frame = program.getMainFrame();
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public Vector2f getDimension() {
		return dimension;
	}

	public Screen getScreen() {
		return screen;
	}

}
