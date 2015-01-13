package program.main;

import com.jme3.app.state.AbstractAppState;
import com.jme3.math.Vector2f;
import core.board.Cell;
import core.graphics.scenes.Scene;

/**
 * Basically just a proxy to some really long method chains
 * @author doc
 */
public class Util {

	// TODO add setScene, setUI, addUI methods

	public static <T extends AbstractAppState> T getUI(String name, Class<T> type){
		return Program.getInstance().getMainFrame().getUIState(name, type);
	}

	public static <T extends Scene> T getScene(String name, Class<T> type){
		return Program.getInstance().getMainFrame().getScene(name, type);
	}

	public static int distance(Cell from, Cell to){
		return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
	}

	// For groovy
	public static Vector2f v2(double x, double y){
		return new Vector2f((float) x, (float) y);
	}
}
