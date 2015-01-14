package shared.items;

import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class Inventory {
	public static final String ITEM_CLASSES_PACKAGE_PATH = "shared.items.types.";

	protected List<Item> items = new LinkedList<Item>();

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
