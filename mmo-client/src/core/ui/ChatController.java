package core.ui;

import core.exceptions.IncorrectHeaderException;
import core.main.DataUtil;
import core.main.Packet;
import program.datastore.DataStore;
import program.datastore.UIStateCondition;
import program.main.Program;
import program.main.Util;

/**
 * @author doc
 */
public class ChatController {

	public void sendMessage(int channelId, String message){
		byte idBytes[] = DataUtil.intToByte(channelId);
		byte messageBytes[] = message.getBytes(Program.UTF_8);
		byte data[] = new byte[idBytes.length + messageBytes.length];
		System.arraycopy(idBytes, 0, data, 0, idBytes.length);
		System.arraycopy(messageBytes, 0, data, idBytes.length, messageBytes.length);

		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_SAY, data));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void messageReceived(final int channelId, final String message){
		DataStore.getInstance().awaitAndExecute(new Runnable() {
			@Override
			public void run() {
				Util.getUI(UI.STATE_CHAT, ChatUIState.class).addChatMessage(channelId, message);
			}
		}, new UIStateCondition(UI.STATE_CHAT));
	}

	public void joinChannel(final int id, final String name){
		DataStore.getInstance().awaitAndExecute(new Runnable() {
			@Override
			public void run() {
				Util.getUI(UI.STATE_CHAT, ChatUIState.class).addChatChannel(id, name);
			}
		}, new UIStateCondition(UI.STATE_CHAT));
	}

	public void leaveChannel(final int id){
		DataStore.getInstance().awaitAndExecute(new Runnable() {
			@Override
			public void run() {
				Util.getUI(UI.STATE_CHAT, ChatUIState.class).removeChatChannel(id);
			}
		}, new UIStateCondition(UI.STATE_CHAT));
	}
}
