package core.ui.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.main.DataUtil;
import core.ui.UI;
import core.ui.map.MapUIState;
import program.main.Util;
import tonegod.gui.core.Screen;

import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class BattleLogUIState extends AbstractAppState {
	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	private BattleLog log;

	public BattleLogUIState(MainFrame frame) {
		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());

		log = new BattleLog(screen, Vector2f.ZERO, DataUtil.parseVector2f("30%, 25%", dimension));
		log.setInitialized();
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		screen.addElement(log);

		if (Util.getUI(UI.STATE_CHAT, MapUIState.class) != null)
			Util.getUI(UI.STATE_CHAT, MapUIState.class).updateLeftSide();
	}

	public BattleLog getLog(){
		return log;
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(log);
	}

	public void log(final String message){
		frame.enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				log.addMsg(message);
				return null;
			}
		});
	}
}