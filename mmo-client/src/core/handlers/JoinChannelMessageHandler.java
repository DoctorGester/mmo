package core.handlers;

import core.main.*;
import program.main.Program;

import java.util.Arrays;

public class JoinChannelMessageHandler extends PacketHandler{

	private Program program;

	public JoinChannelMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		byte[] bytes = data.getData();
		int id = DataUtil.byteToInt(Arrays.copyOf(bytes, 4));
		String name = new String(Arrays.copyOfRange(bytes, 4, bytes.length));
		program.getChatController().joinChannel(id, name);
	}
}
