package program.main;

import com.jme3.app.state.AbstractAppState;
import core.graphics.scenes.Scene;

/**
 * Basically just a proxy to some really long method chains
 * @author doc
 */
public class SceneUtil {

	// TODO add setScene, setUI, addUI methods

	public static <T extends AbstractAppState> T getUI(String name, Class<T> type){
		return Program.getInstance().getMainFrame().getUIState(name, type);
	}

	public static <T extends Scene> T getScene(String name, Class<T> type){
		return Program.getInstance().getMainFrame().getScene(name, type);
	}

}
