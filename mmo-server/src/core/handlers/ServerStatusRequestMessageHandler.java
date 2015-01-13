package core.handlers;

import core.main.*;
import program.main.Program;

public class ServerStatusRequestMessageHandler extends PacketHandler{

	private Program program;

	public ServerStatusRequestMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		if (data.getData().length == 0)
			localServer.send(client, data);
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
