package core.ui;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.ui.map.MapUIState;
import program.main.SceneUtil;
import tonegod.gui.core.Screen;

import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class ChatUIState extends AbstractAppState {
	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	private MainChatBox chatBox;

	public ChatUIState(MainFrame frame) {
		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());

		Vector2f chatBoxSize = dimension.mult(0.30f);
		Vector2f chatBoxPosition = Vector2f.ZERO;

		chatBox = new MainChatBox(screen, chatBoxPosition, chatBoxSize);
		chatBox.setIsMovable(false);
		chatBox.setIsResizable(false);
		chatBox.setIsVisible(false);
		chatBox.setInitialized();
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		screen.addElement(chatBox, true);

		if (SceneUtil.getUI(UI.STATE_CHAT, MapUIState.class) != null)
			SceneUtil.getUI(UI.STATE_CHAT, MapUIState.class).updateLeftSide();
	}

	public MainChatBox getChatBox(){
		return chatBox;
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(chatBox);
	}

	public void addChatMessage(final int channelId, final String message){
		frame.enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				chatBox.receiveMsg(channelId, message);
				return null;
			}
		});
	}

	public void addChatChannel(final int id, final String name) {
		frame.enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				chatBox.addChannel(id, name);
				return null;
			}
		});
	}

	public void removeChatChannel(final int id){
		frame.enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				chatBox.removeChannel(id);
				return null;
			}
		});
	}
}