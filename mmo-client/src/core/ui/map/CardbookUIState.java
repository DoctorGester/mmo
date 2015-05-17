package core.ui.map;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import gui.core.V;
import shared.items.ItemTypes;
import shared.items.filters.TypeFilter;
import core.ui.CloseablePanel;
import core.ui.UI;
import core.ui.UnitCardElement;
import program.main.Program;
import shared.items.types.UnitCardItem;
import tonegod.gui.controls.lists.Slider;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class CardbookUIState  extends AbstractAppState {
	private static final float MAIN_PANEL_WIDTH = 0.6f,
							   MAIN_PANEL_HEIGHT = 0.74f,
							   TOP_PART_HEIGHT = 0.2f,
							   RELATIVE_CARD_HEIGHT = 0.2f,
							   Y_POSITION = 0.14f;

	private int cardsInRow, maxRows;

	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	private CloseablePanel mainPanel;
	private TextField searchInput;
	private Slider pageSlider;
	private Label pageLabel;

	private int page;
	private String searchFilter = "";

	private List<UnitCardElement> cardElements;
	private Vector2f mainPanelSize;

	public CardbookUIState(MainFrame frame) {
		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
		mainPanelSize = new Vector2f(dimension.x * MAIN_PANEL_WIDTH, dimension.y * MAIN_PANEL_HEIGHT);
		mainPanel = new CloseablePanel(screen, Vector2f.ZERO, mainPanelSize){
			public void onClose() {
				CardbookUIState.this.frame.removeUIState(UI.STATE_CARDBOOK);
			}
		};
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		screen.addElement(mainPanel);

		if (mainPanel.getPosition().equals(Vector2f.ZERO))
			slideCenter();

		mainPanel.removeAllChildren();
		mainPanel.setIgnoreMouse(true);

		frame.enqueue(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				reloadFromDeck();
				updateCards();
				return null;
			}
		});

		Label windowLabel = new Label(screen, V.f(mainPanelSize.x * 0.05f, mainPanelSize.y * 0.05f),
											  V.f(mainPanelSize.x * 0.2f, mainPanelSize.y * 0.1f));

		windowLabel.setFontSize(mainPanelSize.y * 0.05f);
		windowLabel.setText("Cardbook");
		mainPanel.addChild(windowLabel);

		searchInput = new TextField(screen, V.f(mainPanelSize.x * 0.7f, mainPanelSize.y * 0.07f),
											V.f(mainPanelSize.x * 0.2f, mainPanelSize.y * 0.05f)){
			@Override
			public void controlKeyPressHook(KeyInputEvent evt, String text) {
				super.controlKeyPressHook(evt, text);
				if (searchFilter.equals(text))
					return;
				searchFilter = text;
				updateCards();
			}
		};
		searchInput.setType(TextField.Type.EXCLUDE_SPECIAL);
		mainPanel.addChild(searchInput);

		pageSlider = new Slider(screen, V.f(mainPanelSize.x * 0.75f, mainPanelSize.y * 0.92f),
										V.f(mainPanelSize.x * 0.2f, mainPanelSize.y * 0.05f),
										Slider.Orientation.HORIZONTAL,
										true) {
			@Override
			public void onChange(int selectedIndex, Object value) {
				setPage((Integer) value);
				updateCards();
			}
		};

		mainPanel.addChild(pageSlider);

		pageLabel = new Label(screen, V.f(mainPanelSize.x * 0.65f, mainPanelSize.y * 0.92f), V.f(mainPanelSize.x * 0.09f, mainPanelSize.y * 0.05f));
		pageLabel.setTextAlign(BitmapFont.Align.Right);
		pageLabel.setTextVAlign(BitmapFont.VAlign.Top);

		mainPanel.addChild(pageLabel);

		float height = mainPanelSize.y * RELATIVE_CARD_HEIGHT;
		float width = height * 0.67f;
		cardsInRow = (int) (mainPanel.getWidth() / width) - 1;
		maxRows = (int) (mainPanel.getHeight() / height) - 2;

		setPage(0);
	}

	private boolean search(String in, String search){
		in = in.toLowerCase();
		search = search.toLowerCase();
		if (in.contains(search))
			return true;

		String acronym = "";
		String words[] = in.split("\\s+");
		for (String word: words)
			acronym += word.charAt(0);

		return acronym.contains(search);
	}

	private Vector2f getCardPosition(int index, float width, float height){
		float allWidth = width * 1.1f * cardsInRow;
		float xOffset = (mainPanel.getWidth() - allWidth) / 2f;
		float yOffset = (mainPanel.getHeight() * TOP_PART_HEIGHT);

		float x = xOffset + (index % cardsInRow) * width * 1.1f,
			  y = mainPanelSize.y - (index / cardsInRow + 1) * height * 1.1f - yOffset;

		return new Vector2f(x, y);
	}

	public void reloadFromDeck(){
		List<UnitCardItem> cards = Program.getInstance().getMainInventory().filter(UnitCardItem.class, new TypeFilter(ItemTypes.CREATURE_CARD));

		cardElements = new ArrayList<UnitCardElement>();

		for (UnitCardItem card: cards){
			float height = mainPanelSize.y * RELATIVE_CARD_HEIGHT;
			UnitCardElement element = new UnitCardElement(screen, getCardPosition(0, height * 0.67f, height), height, card);
			element.setInitialized();
			mainPanel.addChild(element);

			cardElements.add(element);
		}

		int onPage = cardsInRow * maxRows;
		int pages = Math.max(1, cards.size() / onPage);

		pageSlider.setStepIntegerRange(0, pages, 1);
		pageSlider.setIsEnabled(pages > 1);
	}

	// TODO consider making custom slideTo implementations, Effect-based version is pretty buggy
	// TODO rework this
	public void updateCards(){
		int begin = cardsInRow * maxRows * page;
		int end = cardsInRow * maxRows * (page + 1);
		int skipped = 0;

		for (int index = begin; index < end && index < cardElements.size(); index++){
			UnitCardElement element = cardElements.get(index);

			if (!searchFilter.isEmpty() && !search(element.getUnitData().getName(), searchFilter)){
				element.setIsVisible(false);
				skipped++;
				continue;
			}

			element.setIsVisible(true);

			Vector2f to = getCardPosition(index - begin - skipped, element.getWidth(), element.getHeight());
			if (element.getSlideEffect() != null && element.getSlideEffect().getIsActive()){
				element.getSlideEffect().setEffectDestination(to);
			} else {
				Effect slideTo = new Effect(Effect.EffectType.SlideTo, Effect.EffectEvent.Release, .5f);
				slideTo.setElement(element);
				slideTo.setEffectDestination(to);
				element.setSlideEffect(slideTo);
				screen.getEffectManager().applyEffect(slideTo);
			}
		}

		for (int i = 0; i < cardElements.size(); i++){
			if (i >= begin && i < end)
				continue;

			cardElements.get(i).setIsVisible(false);
		}
	}

	public void setPage(int page) {
		this.page = page;
		pageLabel.setText(String.valueOf(page + 1));
	}

	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(mainPanel);
	}

	public void slideRight(){
		mainPanel.setInitialized();
		mainPanel.setPosition(dimension.x * 0.97f - mainPanelSize.x, dimension.y * Y_POSITION);
	}

	public void slideCenter(){
		mainPanel.centerToParent();
		mainPanel.setInitialized();
		mainPanel.setY(dimension.y * Y_POSITION);
	}
}