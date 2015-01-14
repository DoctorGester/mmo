package shared.items.filters;

import shared.items.Item;
import shared.items.ItemFilter;

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
