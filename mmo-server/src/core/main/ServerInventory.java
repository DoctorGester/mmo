package core.main;

import shared.items.Inventory;
import shared.items.Item;
import shared.map.CardMaster;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author doc
 */
public class ServerInventory extends Inventory {
	private CardMaster owner;

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

	public ServerInventory removeItems(List<Item> items){
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

}
