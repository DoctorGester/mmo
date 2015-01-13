package core.handlers;

import core.main.*;
import program.main.Program;

public class CancelTradeMessageHandler extends PacketHandler{
	private Program program;

	public CancelTradeMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		if (data.getData().length != 0)
			return;

		program.getTradingController().cancel(program.findClient(client).getCardMaster());
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
