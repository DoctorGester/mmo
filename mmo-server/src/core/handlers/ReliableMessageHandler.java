package core.handlers;

import core.main.*;
import program.main.ReliablePacketManager;

public class ReliableMessageHandler extends PacketHandler {
	public ReliableMessageHandler(byte[] header) {
		super(header);
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		// Our data is single hash sum int
		if (packet.getData().length == 4)
			ReliablePacketManager.handlePacket(packet);
	}

	public void handle(LocalClient localClient, Packet data) {
	}

}
