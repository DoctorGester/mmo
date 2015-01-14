package core.handlers;

import core.exceptions.IncorrectHeaderException;
import core.main.*;
import shared.items.Item;
import core.main.ItemDatabase;
import program.main.Program;
import shared.other.DataUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class GetItemsMessageHandler extends PacketHandler{
	private static final int MAX_ITEMS = 16;

	private Program program;

	public GetItemsMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		GameClient sender = program.findClient(client);
		
		// Exit if client is not logged in
		if (sender == null)
			return;

		int idArray[] = DataUtil.varIntsToInts(data.getData());

		if (idArray.length > MAX_ITEMS)
			idArray = Arrays.copyOf(idArray, MAX_ITEMS);

		Set<Integer> idSet = new HashSet<Integer>();

		for (int id: idArray)
			idSet.add(id);

		List<Item> items = new ArrayList<Item>();

		for (int id: idSet){
			Item item = ItemDatabase.getInstance().getItemById(id);

			if (item != null)
				items.add(item);
		}

		if (items.size() == 0)
			return;

		// Writing item data
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeByte(items.size());
			for (Item item: items){
				stream.writeInt(item.getId());
				stream.writeUTF(item.getClass().getSimpleName());
				stream.write(item.toBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			localServer.send(client, new Packet(Program.HEADER_GET_ITEMS, bytes.toByteArray()));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
