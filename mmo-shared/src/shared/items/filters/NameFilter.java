package shared.items.filters;

import shared.items.Item;
import shared.items.ItemFilter;

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
