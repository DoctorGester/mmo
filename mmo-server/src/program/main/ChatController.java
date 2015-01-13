package program.main;

import core.main.ChatChannel;
import core.main.DataUtil;
import core.main.GameClient;
import core.main.LocalServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author doc
 */
public class ChatController {
	private int id;
	private Map<Integer, ChatChannel> channels = new HashMap<Integer, ChatChannel>();

	private static LocalServer server = Program.getInstance().getLocalServer();

	private void sendChannelList(GameClient client){
		Set<ChatChannel> channels = client.getChatChannels();

		int ids[] = new int[channels.size()];
		int index = 0;
		for(ChatChannel channel: channels)
			ids[index++] = channel.getId();

		byte data[] = DataUtil.intToVarInt(ids);

		ReliablePacketManager.sendPacket(server, client.getClient(), Program.HEADER_CHANNEL_LIST, data);
	}

	private void sendFullChannelList(GameClient client){
		int ids[] = new int[channels.size()];
		int index = 0;
		for(ChatChannel channel: channels.values())
			ids[index++] = channel.getId();

		byte data[] = DataUtil.intToVarInt(ids);

		ReliablePacketManager.sendPacket(server, client.getClient(), Program.HEADER_ALL_CHANNELS_LIST, data);
	}

	public boolean joinChannel(GameClient client, int channelId){
		ChatChannel channel = channels.get(channelId);

		if (channel == null)
			return false;

		channel.addClient(client);
		client.getChatChannels().add(channel);

		byte id[] = DataUtil.intToByte(channelId);
		byte name[] = channel.getName().getBytes(Program.UTF_8);
		byte send[] = new byte[id.length + name.length];

		System.arraycopy(id, 0, send, 0, id.length);
		System.arraycopy(name, 0, send, id.length, name.length);

		ReliablePacketManager.sendPacket(server, client.getClient(), Program.HEADER_JOIN_CHANNEL, send);

		return true;
	}

	public boolean leaveChannel(GameClient client, int channelId){
		ChatChannel channel = channels.get(channelId);

		if (channel == null)
			return false;

		channel.removeClient(client);
		client.getChatChannels().remove(channel);

		ReliablePacketManager.sendPacket(server, client.getClient(), Program.HEADER_LEAVE_CHANNEL, DataUtil.intToVarInt(channelId));

		return true;
	}

	public ChatChannel createChannel(String name){
		ChatChannel chatChannel = new ChatChannel(id++, name);

		channels.put(chatChannel.getId(), chatChannel);

		return chatChannel;
	}

	public void destroyChannel(int id){
		ChatChannel channel = channels.remove(id);

		byte data[] = DataUtil.intToVarInt(channel.getId());

		for(GameClient client: channel.getClients()){
			ReliablePacketManager.sendPacket(server, client.getClient(), Program.HEADER_LEAVE_CHANNEL, data);
			client.getChatChannels().remove(channel);
		}
	}

	public void messageReceived(int channelId, GameClient from, String message){
		ChatChannel channel = channels.get(channelId);

		if (channel == null)
			return;

		message = from.getCardMaster().getName() + ": " + message;

		channel.sendAll(Program.HEADER_SAY, message.getBytes(Program.UTF_8));
	}
}
