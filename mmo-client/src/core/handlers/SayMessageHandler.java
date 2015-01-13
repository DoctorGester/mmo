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
	}

	public void handle(LocalClient localClient, Packet data) {
		byte bytes[] = data.getData();
		int channelId = DataUtil.byteToInt(Arrays.copyOf(bytes, 4));
		String content = new String(Arrays.copyOfRange(bytes, 4, bytes.length), Program.UTF_8);
		program.getChatController().messageReceived(channelId, content);
	}


}
