package core.handlers;

import core.exceptions.IncorrectHeaderException;
import core.main.*;
import program.main.Program;

import java.util.Arrays;

public class ReliableMessageHandler extends PacketHandler {
	public ReliableMessageHandler(byte[] header) {
		super(header);
	}

	public int getHashSum(byte data[]){
		return Arrays.hashCode(data);
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
	}

	public void handle(LocalClient localClient, Packet data) {
		int hash = getHashSum(data.getRaw());
		try {
			// Receive packet, send it's hash sum back to server
			Packet packet = new Packet(Program.HEADER_RELIABLE, DataUtil.intToByte(hash));
			localClient.send(packet);

			// Handle actual packet data
			localClient.handle(Arrays.copyOfRange(data.getData(), 1, data.getData().length));

		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

}
