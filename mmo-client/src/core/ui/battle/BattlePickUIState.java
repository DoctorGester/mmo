package core.ui.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.math.Vector2f;
import com.jme3.scene.Spatial;
import core.board.Board;
import core.board.Unit;
import core.graphics.MainFrame;
import core.main.CardMaster;
import core.main.DataUtil;
import core.main.inventory.ItemTypes;
import core.main.inventory.filters.TypeFilter;
import core.main.inventory.items.CardItem;
import core.ui.BattleState;
import core.ui.ItemElement;
import core.ui.UnitCardElement;
import program.main.Program;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;
import tonegod.gui.effects.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class BattlePickUIState extends AbstractAppState {
	private static final float PANEL_HEIGHT_PERCENT = 0.2f,
							   PICKED_PANEL_HEIGHT_PERCENT = 0.55f;

	private MainFrame frame;
	private Screen screen;

	private Vector2f dimension;

	private Panel panel, pickedPanel;
	private CardSelectionArea<UnitCardElement> selectionArea;
	private List<ItemElement> pickedCards;

	private Vector2f panelSize, pickedPanelSize;

	private BattleState battleState;

	public BattlePickUIState(MainFrame frame) {
		this.frame = frame;
		this.screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public void setBattleState(BattleState battleState) {
		this.battleState = battleState;
	}

	public void initialize(AppStateManager stateManager, Application app) {
		if (initialized)
			return;

		super.initialize(stateManager, app);

		int panelHeight = (int) (PANEL_HEIGHT_PERCENT * 100);
		Vector2f panelPosition = new Vector2f();
		panelSize = DataUtil.parseVector2f(String.format("100%%, %d%%", panelHeight), dimension);

		panel = new Panel(screen, panelPosition, panelSize);
		panel.setIgnoreMouse(true);

		selectionArea = new CardSelectionArea<UnitCardElement>(screen, new Vector2f(), panelSize);
		panel.addChild(selectionArea);

		screen.addElement(panel);

		int pickedPanelHeight = (int) (PICKED_PANEL_HEIGHT_PERCENT * 100);
		Vector2f pickedPanelPosition = DataUtil.parseVector2f(String.format("0%%, %d%%", panelHeight), dimension);
		pickedPanelSize = DataUtil.parseVector2f(String.format("100%%, %d%%", pickedPanelHeight), dimension);

		pickedPanel = new Panel(screen, pickedPanelPosition, pickedPanelSize);
		pickedPanel.setIsDragDropDropElement(true);
		pickedPanel.setIsMovable(false);
		pickedPanel.setIsResizable(false);

		screen.addElement(pickedPanel);
	}

	public void stateAttached(AppStateManager stateManager){
		if (!initialized)
			initialize(stateManager, stateManager.getApplication());
		updateFromBoard();
	}

	private void updateFromDeck(){
		List<CardItem> cards = Program.getInstance().getMainInventory().filter(CardItem.class, new TypeFilter(ItemTypes.UNIT_CARD));
		for (CardItem card: cards){
			UnitCardElement element = new UnitCardElement(screen, new Vector2f(), panelSize.y * 0.9f, card);
			element.setMouseButtonDispatcher(new UnitCardClickDispatcher(screen, element));
			selectionArea.addCard(element);
		}
	}

	public void updateFromBoard(){
		updateFromDeck();

		List<CardMaster> cardMasters = battleState.getBoard().getCardMasters();
		pickedCards = new ArrayList<ItemElement>(Board.OWNED_UNITS * cardMasters.size());

		float labelOffsetX = pickedPanelSize.x * 0.35f;
		Vector2f size = new Vector2f(dimension.x * 0.2f, dimension.y * 0.1f);

		Vector2f center = new Vector2f(pickedPanelSize.x / 2f - size.x / 2f,
									   pickedPanelSize.y / 2f - size.y / 2f);

		float cardHeight = panelSize.y * 0.9f;
		int id = 0;
		for(CardMaster cardMaster: cardMasters){
			int row = id / 2;

			int side = (id % 2 == 0) ? 1 : -1;

			int masters = cardMasters.size();
			if (masters % 2 != 0)
				masters++;
			float rowCenter = masters / 4f - 0.5f;
			float rowOffset = row - rowCenter;

			Vector2f pos = center.subtract(side * labelOffsetX, rowOffset * cardHeight * 1.5f);
			Label name = new Label(screen, pos, size);
			name.setText(cardMaster.getName());
			name.setInitialized();
			name.setFontColor(Board.PLAYER_COLORS[cardMaster.getBattleId()]);
			if (side == -1)
				name.setTextAlign(BitmapFont.Align.Right);

			pickedPanel.addChild(name);

			id++;
		}
	}

	private Vector2f getNextCardPosition(ItemElement itemElement){
		Board board = battleState.getBoard();
		CardMaster owner = board.getUnits().get(board.getUnits().size() - 1).getOwner();
		int id = owner.getBattleId();
		int side = (id % 2 == 0) ? 1 : -1;

		// Calculating rowOffset
		int row = id / 2;

		int masters = board.getCardMasters().size();
		if (masters % 2 != 0)
			masters++;
		float rowCenter = masters / 4f - 0.5f;
		float rowOffset = row - rowCenter;

		// Getting unit amount owned by player
		int amount = 0;
		for (Unit unit: board.getUnits())
			if (unit.getOwner() == owner)
				amount++;

		// Returning final values
		Vector2f center = new Vector2f(pickedPanelSize.x / 2f - itemElement.getWidth() / 2f,
									   pickedPanelSize.y / 2f - itemElement.getHeight() / 2f);
		return center.subtractLocal(side * amount * itemElement.getWidth() * 1.1f,
									rowOffset * itemElement.getHeight() * 1.5f);
	}

	public void addPickedCard(final CardItem card){
		frame.enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				UnitCardElement cardElement = null;
				for (UnitCardElement element: selectionArea.getCards())
					if (element.getCardId() == card.getId())
						cardElement = element;

				if (cardElement != null){
					selectionArea.removeCard(cardElement);

					pickedPanel.addChild(cardElement);
					// Correcting height according to the new parent
					cardElement.setY(cardElement.getY() + pickedPanel.getHeight());
					cardElement.setIsMovable(false);
					cardElement.setIsDragDropDragElement(false);
				} else {
					Vector2f start = new Vector2f(pickedPanel.getWidth(), pickedPanel.getHeight());

					cardElement = new UnitCardElement(screen, start, panelSize.y * 0.9f, card);
					cardElement.setMouseButtonDispatcher(new UnitCardClickDispatcher(screen, cardElement));
					pickedPanel.addChild(cardElement);
				}

				Vector2f to = getNextCardPosition(cardElement);
				Effect slideTo = new Effect(Effect.EffectType.SlideTo, Effect.EffectEvent.Release, .45f);
				slideTo.setElement(cardElement);
				slideTo.setEffectDestination(to);
				cardElement.setSlideEffect(slideTo);
				screen.getEffectManager().applyEffect(slideTo);
				pickedCards.add(cardElement);
				return null;
			}
		});
	}

	public boolean isCardPlacementFinished(){
		for (Spatial spatial: pickedPanel.getChildren()){
			if (spatial instanceof ItemElement){
				ItemElement element = (ItemElement) spatial;
				Effect slide = element.getSlideEffect();
				if (slide == null || slide.getIsActive())
					return false;
			}
		}
		return true;
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(panel);
		screen.removeElement(pickedPanel);
	}

	public void update(float tpf){
		for (ItemElement card: pickedCards)
			if (card.isBeingFlipped())
				card.updateFlip();
	}
}
