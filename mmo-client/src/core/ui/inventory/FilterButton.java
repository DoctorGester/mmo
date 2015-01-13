package core.ui.inventory;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import core.main.inventory.ItemFilter;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.core.ElementManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author doc
 */
public class FilterButton extends ButtonAdapter {
	private List<ItemFilter> filters;
	private FilterListener filterListener;

	public FilterButton(ElementManager screen, Vector2f position, Vector2f dimensions, ItemFilter ... filters) {
		super(screen, position, dimensions);
		this.filters = Arrays.asList(filters);
	}

	public List<ItemFilter> getFilters() {
		return filters;
	}

	public ItemFilter[] getFilterArray() {
		return filters.toArray(new ItemFilter[filters.size()]);
	}

	public void setFilterListener(FilterListener filterListener) {
		this.filterListener = filterListener;
	}

	public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
		filterListener.buttonPressed(this);
	}
}
