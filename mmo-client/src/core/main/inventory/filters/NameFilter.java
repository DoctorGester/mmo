package core.main.inventory.filters;

import core.main.inventory.Item;
import core.main.inventory.ItemFilter;

/**
 * @author doc
 */
public class NameFilter implements ItemFilter {
	private String searchString;

	public NameFilter(String searchString) {
		this.searchString = searchString;
	}

	@Override
	public boolean doesItemPass(Item item) {
		String in = item.getName().toLowerCase();
		String search = searchString.toLowerCase();
		if (in.contains(search))
			return true;

		String acronym = "";
		String words[] = in.split("\\s+");
		for (String word: words)
			acronym += word.charAt(0);

		return acronym.contains(search);
	}
}
