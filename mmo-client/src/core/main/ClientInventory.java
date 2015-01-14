package core.main;

import core.main.ItemDatabase;
import shared.items.Inventory;
import shared.items.Item;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class ClientInventory extends Inventory {
	private List<Item> items = new LinkedList<Item>();

	public void fromBytes(byte data[]){
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
		DataInputStream stream = new DataInputStream(byteInputStream);

		try {
			items.clear();

			short length = stream.readShort();

			for (int i = 0; i < length; i++){
				int id = stream.readInt();
				String className = ITEM_CLASSES_PACKAGE_PATH + stream.readUTF();

				Class itemClass = Class.forName(className);

				Item existed = ItemDatabase.getInstance().getItem(id);

				if (existed != null){
					items.add(existed);
					continue;
				}

				Object instance = itemClass.newInstance();
				if (instance instanceof Item){
					Item item = (Item) instance;
					item.setId(id);

					items.add(item);

					ItemDatabase.getInstance().addItem(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
