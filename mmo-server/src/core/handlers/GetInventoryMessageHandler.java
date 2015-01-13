package core.handlers;

import core.main.*;
import program.main.Program;
import program.main.ReliablePacketManager;

public class GetInventoryMessageHandler extends PacketHandler{

	private Program program;

	public GetInventoryMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		GameClient sender = program.findClient(client);
		
		// Exit if client is not logged in
		if (sender == null)
			return;

		ReliablePacketManager.sendPacket(localServer, client, Program.HEADER_GET_INVENTORY, sender.getCardMaster().getInventory().toBytes());
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
