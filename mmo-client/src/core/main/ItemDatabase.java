package core.main;

import core.exceptions.IncorrectHeaderException;
import program.datastore.DataStore;
import program.datastore.Subscriber;
import program.main.Program;
import shared.items.Inventory;
import shared.items.Item;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ItemDatabase {
	private static final String DATA_STORE_KEY = "itemDB.";

	private static ItemDatabase instance = new ItemDatabase();

	private Map<Integer, Item> cache = new HashMap<Integer, Item>();
	private final List<Item> requestedItems = new ArrayList<Item>();

	public static ItemDatabase getInstance(){
		return instance;
	}

	private ItemDatabase(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new RequestItemsTask(), 1, 1, TimeUnit.SECONDS);
	}

	public void addItem(Item item){
		cache.put(item.getId(), item);
	}

	public void subscribe(int id, Subscriber subscriber){
		DataStore.getInstance().subscribe(DATA_STORE_KEY + id, subscriber);
	}

	public void itemReceived(DataInputStream stream){
		try {
			int id = stream.readInt();
			String className = Inventory.ITEM_CLASSES_PACKAGE_PATH + stream.readUTF();
			Class<? extends Item> itemClass = Class.forName(className).asSubclass(Item.class);

			Item item = getOrCreateItem(id, itemClass);
			item.fromBytes(stream, true);

			DataStore.getInstance().put(DATA_STORE_KEY + id, item);

			synchronized (requestedItems){
				requestedItems.remove(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void requestItem(Item item){
		synchronized (requestedItems){
			requestedItems.add(item);
		}
	}

	public Item getItem(int id){
		return cache.get(id);
	}

	public <T extends Item> T getOrCreateItem(int id, Class<T> type) {
		try {
			Item item = getItem(id);

			if (item == null){
				Object instance = type.newInstance();
				item = (Item) instance;
				item.setId(id);

				addItem(item);
			}

			return type.cast(item);
		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new RuntimeException("Unable to create item with id " + id + " and class " + type);
	}

	private class RequestItemsTask implements Runnable{

		@Override
		public void run() {
			try {
				synchronized (requestedItems){
					int index = 0;
					int missingItems[] = new int[requestedItems.size()];

					for (Item item: requestedItems)
						if (DataStore.getInstance().get(DATA_STORE_KEY + item.getId()) == null)
							missingItems[index++] = item.getId();

					missingItems = Arrays.copyOf(missingItems, index);

					if (missingItems.length > 0)
						requestItems(missingItems);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void requestItems(int idArray[]) throws IncorrectHeaderException {
		Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_GET_ITEMS, DataUtil.intToVarInt(idArray)));
	}
}
