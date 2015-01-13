package core.main.inventory;

import core.main.CardMaster;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class Inventory {
	private CardMaster owner;
	private List<Item> items = new LinkedList<Item>();

	public CardMaster getOwner() {
		return owner;
	}

	public void setOwner(CardMaster owner) {
		this.owner = owner;

		for (Item item: items)
			item.setOwner(owner);
	}

	public void updateItems(){
		for (Item item: items)
			ItemDatabase.getInstance().updateItem(item);
	}

	public Inventory addItems(List<Item> items){
		this.items.addAll(items);

		for (Item item: items)
			item.setOwner(owner);

		return this;
	}

	public Inventory setItems(List<Item> items) {
		for (Item item: this.items)
			item.setOwner(null);

		this.items.clear();
		this.items.addAll(items);

		for (Item item: this.items)
			item.setOwner(owner);

		return this;
	}

	public Inventory removeItems(List<Item> items){
		for (Item item: items)
			item.setOwner(null);

		this.items.removeAll(items);

		return this;
	}

	public void loadItems(){
		setItems(ItemDatabase.getInstance().getItemsOfOwner(owner.getId()));
	}

	public byte[] toBytes(){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeShort(items.size());
			for (Item item: items){
				stream.writeInt(item.getId());
				stream.writeUTF(item.getClass().getSimpleName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bytes.toByteArray();
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
