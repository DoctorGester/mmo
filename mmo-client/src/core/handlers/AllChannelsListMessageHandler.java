package core.handlers;

import core.main.*;
import program.main.Program;

public class AllChannelsListMessageHandler extends PacketHandler{

	private Program program;

	public AllChannelsListMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
