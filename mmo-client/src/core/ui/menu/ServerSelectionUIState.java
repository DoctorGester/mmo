package core.ui.menu;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.main.DataUtil;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class ServerSelectionUIState extends AbstractAppState {
	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	private static Map<String, InetSocketAddress> serverList;

	private Panel panel;

	public ServerSelectionUIState(MainFrame frame) {
		serverList = new HashMap<String, InetSocketAddress>();

		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());

		addServer("Local Server", "127.0.0.1", 3637);
		addServer("dglab Realm", "94.242.199.108", 3637);
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		Vector2f panelPosition = DataUtil.parseVector2f("65%, 30%", dimension),
				 panelSize = DataUtil.parseVector2f("30%, 60%", dimension);
		panel = new Panel(screen, panelPosition, panelSize);
		panel.setIgnoreMouse(true);
		panel.setText("Select a server");
		panel.setTextAlign(BitmapFont.Align.Center);
		panel.setTextPadding(5);

		int index = 0;
		for (Map.Entry<String, InetSocketAddress> entry: serverList.entrySet()){
			int y = 15 + index * 12;
			Vector2f buttonPosition = DataUtil.parseVector2f("10%," + y + "%", panelSize),
					 buttonSize = DataUtil.parseVector2f("80%, 10%", panelSize);
			Button button = new ServerSelectionButton(screen, buttonPosition, buttonSize, entry.getValue());
			button.setText(entry.getKey());

			panel.addChild(button);
			index++;
		}

		screen.addElement(panel);
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(panel);
	}

	public static void addServer(String name, String address, int port){
		serverList.put(name, new InetSocketAddress(address, port));
	}
}
