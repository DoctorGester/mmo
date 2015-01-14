package core.handlers;

import core.main.*;
import program.datastore.DataKey;
import program.datastore.DataStore;
import program.datastore.ExistenceCondition;
import program.main.Program;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class GetItemsMessageHandler extends PacketHandler{

	public GetItemsMessageHandler(byte header[]){
		super(header);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet packet) {
		DataInputStream stream = DataUtil.stream(packet.getData());

		try {
			int itemAmount = stream.readByte();

			for (int i = 0; i < itemAmount; i++)
				ItemDatabase.getInstance().itemReceived(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
