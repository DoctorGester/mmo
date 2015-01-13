package core.main.inventory.filters;

import core.main.inventory.Item;
import core.main.inventory.ItemFilter;

/**
 * @author doc
 */
public class TypeFilter implements ItemFilter {
	private int type;

	public TypeFilter(int type) {
		this.type = type;
	}

	@Override
	public boolean doesItemPass(Item item) {
		return item.getType() == type;
	}
}
