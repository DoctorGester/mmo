package core.handlers;

import core.main.*;
import program.main.Program;

import java.util.Arrays;

public class SayMessageHandler extends PacketHandler{

	private Program program;

	public SayMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		byte[] bytes = data.getData();
		int channelId = DataUtil.byteToInt(Arrays.copyOf(bytes, 4));
		GameClient sender = program.findClient(client);
		String message = new String(Arrays.copyOfRange(bytes, 4, bytes.length), Program.UTF_8);
		program.getChatController().messageReceived(channelId, sender, message);
		if (message.equals("save"))
			try {
				Program.getInstance().getMapController().saveCardMaster(sender.getCardMaster());
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
