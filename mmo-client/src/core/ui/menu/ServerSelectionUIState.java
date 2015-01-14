package core.ui.menu;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import gui.core.V;
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

		Vector2f panelPosition = V.f(dimension.x * 0.65f, dimension.y * 0.3f),
				 panelSize = V.f(dimension.x * 0.3f, dimension.y * 0.6f);
		panel = new Panel(screen, panelPosition, panelSize);
		panel.setIgnoreMouse(true);
		panel.setText("Select a server");
		panel.setTextAlign(BitmapFont.Align.Center);
		panel.setTextPadding(5);

		int index = 0;
		for (Map.Entry<String, InetSocketAddress> entry: serverList.entrySet()){
			float y = 0.15f + index * 0.12f;
			Vector2f buttonPosition = V.f(panelSize.x * 0.1f, panelSize.y * y),
					 buttonSize = V.f(panelSize.x * 0.8f, panelSize.y * 0.1f);
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
