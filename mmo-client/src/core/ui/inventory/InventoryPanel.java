package core.ui.inventory;

import com.jme3.font.BitmapFont;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import shared.items.Inventory;
import shared.items.Item;
import shared.items.ItemFilter;
import shared.items.ItemTypes;
import shared.items.filters.TypeFilter;
import core.ui.ItemElement;
import core.ui.Pager;
import core.ui.SpellCardElement;
import core.ui.UnitCardElement;
import program.datastore.Data;
import program.datastore.DataKey;
import program.datastore.DataStore;
import program.main.Program;
import shared.items.types.UnitCardItem;
import shared.items.types.SpellCardItem;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.utils.UIDUtil;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class InventoryPanel extends Element implements Control {
	private static final float PANEL_WIDTH = 0.3f,
							   PANEL_HEIGHT = 0.68f,
							   RELATIVE_CARD_HEIGHT = 0.2f,
							   ELEMENT_AREA_OFFSET = 0.15f,
							   ELEMENT_AREA_SIZE = 0.7f;


	private static final float CARD_MOVING_SPEED_PERCENT = 0.02f;

	private final Vector2f dimension;
	private final Vector2f panelSize;

	private Inventory inventory;

	private FilterButton activeFilterButton;
	private List<FilterButton> filterButtons = new ArrayList<FilterButton>();

	private List<ItemElement> items = new LinkedList<ItemElement>();
	private Map<Item, ItemElement> elementCache = new HashMap<Item, ItemElement>();

	private Pager pager;

	private int cardsInRow, maxRows;

	private long lastUpdateTime;

	public InventoryPanel(ElementManager screen) {
		super(screen, UIDUtil.getUID(), Vector2f.ZERO, Vector2f.ZERO, Vector4f.ZERO, null);

		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
		panelSize = new Vector2f(dimension.x * PANEL_WIDTH, dimension.y * PANEL_HEIGHT);

		setDimensions(screen.getWidth() * PANEL_WIDTH, screen.getHeight() * PANEL_HEIGHT);

		setIgnoreMouse(true);
		setInitialized();

		createFilterButtons();

		pager = new InventoryPager(screen, panelSize.mult(0.04f), new Vector2f(panelSize.x * 0.5f, panelSize.y * 0.075f));
		addChild(pager);
		pager.centerToParentH();

		addControl(this);

		// Calculating rows and columns
		float height = panelSize.y * RELATIVE_CARD_HEIGHT;
		float width = height * 0.67f;
		cardsInRow = (int) (panelSize.x / width) - 1;
		maxRows = (int) (panelSize.y * ELEMENT_AREA_SIZE / height);
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	private void createFilterButton(String name, ItemFilter... filters){
		float padding = panelSize.x * 0.04f;

		FilterButton button = new FilterButton(screen, Vector2f.ZERO, Vector2f.ZERO, filters);
		button.setText(name);
		button.setTextVAlign(BitmapFont.VAlign.Center);
		button.setFilterListener(new FilterListener() {
			@Override
			public void buttonPressed(FilterButton button) {
				activeFilterButton = button;
				pager.setCurrentPage(0);
			}
		});

		filterButtons.add(button);

		addChild(button);

		// Updating button sizes and positions
		int index = 0;

		for (FilterButton filterButton: filterButtons){
			Vector2f size = new Vector2f((panelSize.x - padding * 2 - (filterButtons.size() - 1) * padding) / filterButtons.size(), panelSize.y * 0.07f);
			Vector2f position = new Vector2f(padding + ((size.x + padding) * index), panelSize.getY() - size.y - padding);

			filterButton.setDimensions(size);
			filterButton.setPosition(position);
			filterButton.setText(filterButton.getText()); // Updating text position, 'cause it's not updated at the size change

			index++;
		}
	}

	private void createFilterButtons(){
		createFilterButton("Creatures", new TypeFilter(ItemTypes.CREATURE_CARD));
		createFilterButton("Spells", new TypeFilter(ItemTypes.SPELL_CARD));
		createFilterButton("Items");

		activeFilterButton = filterButtons.get(0);
	}

	private void reloadWithPage(int page){
		lastUpdateTime = System.currentTimeMillis();

		for (ItemElement element: items)
			removeChild(element);

		items.clear();

		List<Item> filteredItems = inventory.filter(activeFilterButton.getFilterArray());

		int onPage = cardsInRow * maxRows;
		int pages = Math.max(1, (int) Math.ceil(filteredItems.size() / (double) onPage));

		pager.setMaxPages(pages);

		int index = -1;
		int begin = cardsInRow * maxRows * page;
		int end = cardsInRow * maxRows * (page + 1);

		for (Item item: filteredItems){
			index++;

			if (index < begin || index >= end)
				continue;

			ItemElement element = getItemElement(item);

			addChild(element);
			items.add(element);
		}
	}

	private void checkForUpdate(){
		Data data = DataStore.getInstance().getData(DataKey.INVENTORY);

		if (data == null)
			return;

		if (data.getDate() > lastUpdateTime)
			pager.setCurrentPage(pager.getCurrentPage());
	}

	@Override
	public void update(float tpf){
		checkForUpdate();

		float cardMovingSpeed = screen.getWidth() * CARD_MOVING_SPEED_PERCENT;

		for (int i = 0; i < items.size(); i++){
			ItemElement itemElement = items.get(i);

			if (itemElement.isBeingDragged() || !itemElement.isEnabled())
				continue;

			Vector2f toPosition = getCardPosition(i, itemElement.getWidth(), itemElement.getHeight());
			Vector2f currentPosition = itemElement.getPosition();

			if (toPosition.subtract(currentPosition).length() < cardMovingSpeed){
				itemElement.setPosition(toPosition);
				continue;
			}

			Vector2f to = toPosition.subtract(currentPosition).normalizeLocal().multLocal(cardMovingSpeed);

			itemElement.setPosition(currentPosition.add(to));
		}
	}

	private Vector2f getCardPosition(int index, float width, float height){
		float allWidth = width * 1.1f * cardsInRow;
		float xOffset = (panelSize.x - allWidth) / 2f;
		float yOffset = (panelSize.y * ELEMENT_AREA_OFFSET);

		float x = xOffset + (index % cardsInRow) * width * 1.1f,
				y = panelSize.y - (index / cardsInRow + 1) * height * 1.1f - yOffset;

		return new Vector2f(x, y);
	}

	private ItemElement getItemElement(Item item){
		if (elementCache.containsKey(item))
			return elementCache.get(item);

		ItemElement element;

		if (item instanceof UnitCardItem){
			element = new UnitCardElement(screen, Vector2f.ZERO, panelSize.y * RELATIVE_CARD_HEIGHT, (UnitCardItem) item);
		} else if (item instanceof SpellCardItem) {
			element = new SpellCardElement(screen, Vector2f.ZERO, panelSize.y * RELATIVE_CARD_HEIGHT, (SpellCardItem) item);
		} else {
			element = new ItemElement(screen, Vector2f.ZERO, panelSize.y * RELATIVE_CARD_HEIGHT);
		}

		elementCache.put(item, element);

		return element;
	}

	private class InventoryPager extends Pager{

		public InventoryPager(ElementManager screen, Vector2f position, Vector2f dimensions) {
			super(screen, position, dimensions);
		}

		@Override
		public void pageChanged(int from, final int to) {
			if (inventory != null)
				Program.getInstance().getMainFrame().enqueue(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						reloadWithPage(to - 1);
						return null;
					}
				});

		}
	}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		return this;
	}

	@Override
	public void setSpatial(Spatial spatial) {}

	@Override
	public void render(RenderManager rm, ViewPort vp) {}
}
