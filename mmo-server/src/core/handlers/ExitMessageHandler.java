package core.handlers;

import program.main.Program;
import core.main.Client;
import core.main.LocalClient;
import core.main.LocalServer;
import core.main.Packet;
import core.main.PacketHandler;

public class ExitMessageHandler extends PacketHandler{

	private Program program;

	public ExitMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		if (client.getAddress().getHostAddress().equals("127.0.0.1"))
			System.exit(0);
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
