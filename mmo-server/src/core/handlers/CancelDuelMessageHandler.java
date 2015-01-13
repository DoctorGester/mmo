package core.handlers;

import core.main.*;
import program.main.Program;

public class CancelDuelMessageHandler extends PacketHandler{
	private Program program;

	public CancelDuelMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		if (data.getData().length != 0)
			return;

		program.getDuelController().cancel(program.findClient(client).getCardMaster());
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
