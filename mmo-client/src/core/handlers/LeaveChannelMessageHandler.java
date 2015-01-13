package core.handlers;

import core.main.*;
import program.main.Program;

public class LeaveChannelMessageHandler extends PacketHandler{

	private Program program;

	public LeaveChannelMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		program.getChatController().leaveChannel(DataUtil.varIntsToInts(data.getData())[0]);
	}


}
