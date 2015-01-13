package core.handlers;

import core.main.*;
import program.datastore.DataKey;
import program.datastore.DataStore;
import program.datastore.ExistenceCondition;
import program.main.Program;

public class GetInventoryMessageHandler extends PacketHandler{

	private Program program;

	public GetInventoryMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet packet) {
		final byte data[] = packet.getData().clone();
		DataStore.getInstance().awaitAndExecute(new Runnable() {
			@Override
			public void run() {
				if (data.length != 0){
					program.getMainInventory().fromBytes(data);
					DataStore.getInstance().put(DataKey.INVENTORY, data);
				}
			}
		}, new ExistenceCondition(DataKey.MAIN_PLAYER));
	}
}
