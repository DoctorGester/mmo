package core.handlers;

import program.main.Program;
import core.main.Client;
import core.main.GameClient;
import core.main.LocalClient;
import core.main.LocalServer;
import core.main.Packet;
import core.main.PacketHandler;

public class StillOnlineMessageHandler extends PacketHandler{

	private Program program;

	public StillOnlineMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		GameClient gc = program.findClient(client);
		if (gc != null)
			gc.updateOnline();
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
