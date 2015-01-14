package core.main;

import program.main.database.Database;
import program.main.Program;
import program.main.database.entities.CardMasterEntity;
import program.main.database.entities.ItemEntity;
import shared.items.Item;

import java.io.InvalidClassException;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class ItemDatabase {
	private static ItemDatabase instance = new ItemDatabase();

	private Map<Integer, Item> cache = new HashMap<Integer, Item>();

	public static ItemDatabase getInstance(){
		return instance;
	}

	private ItemDatabase(){}

	public void updateItem(Item item){
		Database db = Program.getInstance().getDatabase();

		try {
			ItemEntity entity = db.getItemDao().queryForId(item.getId());

			entity.setOwner(db.getCardMasterDao().queryForId(item.getOwnerId()));
			entity.setData(item.toBytes());

			db.getItemDao().update(entity);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int registerItem(Item item){
		Database db = Program.getInstance().getDatabase();

		try {
			ItemEntity entity = new ItemEntity();

			entity.setType(item.getClass().getName());
			entity.setOwner(db.getCardMasterDao().queryForId(item.getOwnerId()));
			entity.setData(item.toBytes());

			db.getItemDao().create(entity);

			item.setId(entity.getId());

			cache.put(item.getId(), item);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return item.getId();
	}

	private Item createItemFromEntity(ItemEntity entity) throws Exception {
		Class itemClass = Class.forName(entity.getType());

		Object instance = itemClass.newInstance();
		if (instance instanceof Item){
			Item item = (Item) instance;
			item.setId(entity.getId());
			item.fromBytes(entity.getData(), false);

			cache.put(item.getId(), item);

			return item;
		}

		throw new InvalidClassException(itemClass.getName(), "Provided class is not an instance of Item");
	}

	public List<Item> getItemsOfOwner(int ownerId){
		GameClient ownerClient = Program.getInstance().getGameClientById(ownerId);
		List<Item> items = new LinkedList<Item>();

		try {
			Database db = Program.getInstance().getDatabase();

			List<ItemEntity> entities = db.getItemDao().queryForEq(ItemEntity.OWNER_ID_FIELD, ownerId);

			for (ItemEntity entity: entities){
				Item item = createItemFromEntity(entity);
				item.setOwner(ownerClient == null ? null : ownerClient.getCardMaster());

				items.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return items;
	}

	public Item getItemById(int itemId){
		if (cache.containsKey(itemId))
			return cache.get(itemId);

		try {
			Database db = Program.getInstance().getDatabase();

			ItemEntity entity = db.getItemDao().queryForId(itemId);

			if (entity != null){
				Item item = createItemFromEntity(entity);

				// Setting owner
				CardMasterEntity owner = entity.getOwner();

				if (owner != null){
					GameClient ownerClient = Program.getInstance().getGameClientById(owner.getId());
					item.setOwner(ownerClient == null ? null : ownerClient.getCardMaster());
				}

				return item;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
