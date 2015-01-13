package core.ui;

import com.jme3.input.KeyInput;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import core.exceptions.IncorrectHeaderException;
import core.main.Packet;
import program.main.Program;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.ComboBox;
import tonegod.gui.controls.scrolling.ScrollArea;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class MainChatBox extends Panel {
	private static final Vector2f INSETS = new Vector2f(10f, 10f);
	private static final float textFieldHeight = 24f;

	private ComboBox channelList;
	private TextField textField;
	private Button sendButton;
	private ChatChannel currentChannel;

	private Map<Integer, ChatChannel> channels = new HashMap<Integer, ChatChannel>();

	public MainChatBox(Screen screen, Vector2f position, Vector2f dimensions) {
		super(screen, position, dimensions);

		channelList = new ComboBox(screen, Vector2f.ZERO) {
			@Override
			public void onChange(int selectedIndex, Object value) {
				changeChannel(Integer.valueOf(value.toString()));
			}
		};
		channelList.setIsEnabled(false); // Disabling editing
		channelList.setInitialized();

		textField = new TextField(screen, Vector2f.ZERO){
			@Override
			public void controlKeyPressHook(KeyInputEvent evt, String text) {
				if (evt.isPressed() && evt.getKeyCode() == KeyInput.KEY_RETURN)
					sendPressed();
			}
		};

		sendButton = new ButtonAdapter(screen, Vector2f.ZERO){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				sendPressed();
			}
		};

		addChild(channelList);
		addChild(textField);
		addChild(sendButton);

		updateFromSize(dimensions);
	}

	private void updateFromSize(Vector2f dimensions){
		Vector2f channelListPosition = new Vector2f(INSETS.x, dimensions.y - channelList.getHeight() - INSETS.y);

		Vector2f fieldSize = new Vector2f(dimensions.x * 0.7f - INSETS.x, textFieldHeight),
				 fieldPosition = new Vector2f(INSETS.x, INSETS.y);

		Vector2f buttonSize = new Vector2f(dimensions.x * 0.3f - INSETS.x * 2f, fieldSize.y),
				 buttonPosition = new Vector2f(fieldPosition.x + fieldSize.x + INSETS.x, fieldPosition.y);

		textField.setDimensions(fieldSize);
		textField.setPosition(fieldPosition);
		textField.setFontSize(fieldSize.y / 1.5f);
		sendButton.setDimensions(buttonSize);
		sendButton.setPosition(buttonPosition);
		sendButton.setFontSize(buttonSize.y / 1.5f);
		sendButton.setText("Send");
		channelList.setPosition(channelListPosition);
	}

	public void sendPressed() {
		try {
			String msg = textField.getText();
			textField.setText("");

			if (msg.startsWith("/q ")){
				byte data[] = msg.substring(3).getBytes(Program.UTF_8);
				Packet packet = new Packet(Program.HEADER_QUERY, data);
				Program.getInstance().getLocalClient().send(packet);
				return;
			}
			Program.getInstance().getChatController().sendMessage(currentChannel.id, msg);
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void addChannel(int id, String name){
		ChatChannel get = channels.get(id);

		if (get != null)
			return;

		ChatChannel channel = new ChatChannel(id, name);

		channels.put(id, channel);
		channelList.addListItem(name, id);

		if (channels.size() == 1)
			changeChannel(id);
	}

	public void receiveMsg(int channel, String message){
		ChatChannel get = channels.get(channel);

		if (get == null)
			return;

		get.receiveMsg(message);
		get.rebuild();
	}

	public void removeChannel(int id){
		channels.remove(id);

		channelList.removeListItem(Integer.valueOf(id));

		// Changing channel to first available, if present
		if (currentChannel.id == id && channels.size() > 0)
			channelList.setSelectedByValue(channels.values().iterator().next().id, true);
	}

	private void changeChannel(int toId){
		ChatChannel chatChannel = channels.get(toId);
		if (currentChannel != null)
			removeChild(currentChannel.scrollArea);

		currentChannel = chatChannel;
		addChild(currentChannel.scrollArea);

		setIsVisible(isVisible);
	}

	public void setDimensions(Vector2f dimensions){
		super.setDimensions(dimensions);

		updateFromSize(dimensions);

		for (ChatChannel channel: channels.values())
			channel.updateSize();
	}

	private class ChatChannel{
		private int id;
		private String name;
		private List<String> history = new LinkedList<String>();
		private ScrollArea scrollArea;

		private ChatChannel(int id, String name) {
			this.id = id;
			this.name = name;

			scrollArea = new ScrollArea(screen, Vector2f.ZERO, Vector2f.ZERO, Vector4f.ZERO, null, true);
			scrollArea.setInitialized();

			updateSize();

			scrollArea.setIsResizable(false);
			scrollArea.setFontSize(getDimensions().y * 0.07f);
			scrollArea.setText("");
			scrollArea.scrollToBottom();
		}

		private void updateSize(){
			Vector2f size = getDimensions().subtract(INSETS.x, channelList.getHeight() + textFieldHeight),
					 position = new Vector2f(INSETS.x, INSETS.y * 2);

			scrollArea.setDimensions(size);
			scrollArea.setPosition(position);
		}

		public void receiveMsg(String message){
			history.add(message);
		}

		public void rebuild(){
			String displayText = "";
			int index = 0;
			for (String s: history) {
				if (index > 0)
					displayText += "\n" + s;
				else
					displayText += s;
				index++;
			}
			scrollArea.setText(displayText);
			scrollArea.scrollToBottom();
		}
	}
}
