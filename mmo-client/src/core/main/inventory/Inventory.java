package core.main.inventory;

import core.main.ItemDatabase;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class Inventory {
	public static final String ITEM_CLASSES_PACKAGE_PATH = "core.main.inventory.items.";

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

	public List<Item> filter(ItemFilter ... filters){
		List<Item> items = new LinkedList<Item>();

		for (Item item: this.items){
			boolean pass = true;
			for (ItemFilter filter: filters)
				if (!filter.doesItemPass(item)){
					pass = false;
					break;
				}

			if (pass)
				items.add(item);
		}

		return items;
	}

	public <T extends Item> List<T> filter(Class<T> type, ItemFilter ... filters){
		List<Item> filtered = filter(filters);
		List<T> items = new LinkedList<T>();

		for (Item item: filtered)
			if (type.isInstance(item))
				items.add(type.cast(item));

		return items;
	}

	public Item findById(int id){
		for (Item item: items)
			if (item.getId() == id)
				return item;

		return null;
	}

	public <T extends Item> T findById(int id, Class<T> type){
		Item item = findById(id);

		if (item == null || !type.isInstance(item))
			return null;

		return type.cast(item);
	}
}
