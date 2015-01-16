package core.ui.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import gui.core.V;
import shared.board.Board;
import shared.board.Unit;
import shared.items.ItemTypes;
import shared.items.filters.TypeFilter;
import core.ui.SpellCardElement;
import core.ui.UI;
import program.main.Program;
import program.main.SceneUtil;
import shared.items.types.SpellCardItem;
import shared.map.CardMaster;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;

import java.util.List;

/**
 * @author doc
 */
public class SpellSelectorUIState extends AbstractAppState {
	private static final float PANEL_HEIGHT_PERCENT = 0.2f,
							   DROP_PANEL_HEIGHT_PERCENT = 0.55f;
	private MainFrame frame;
	private Screen screen;

	private Vector2f dimension;

	private Panel panel, dropPanel;
	private CardSelectionArea<SpellCardElement> selectionArea;

	private Vector2f panelSize;

	public SpellSelectorUIState(MainFrame frame) {
		this.frame = frame;
		this.screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());

		Vector2f panelPosition = new Vector2f();
		panelSize = V.f(dimension.x, dimension.y * PANEL_HEIGHT_PERCENT);

		panel = new Panel(screen, panelPosition, panelSize);
		panel.setIgnoreMouse(true);

		Vector2f dropPanelPosition = V.f(0, dimension.y * PANEL_HEIGHT_PERCENT);
		Vector2f dropPanelSize = V.f(dimension.x, dimension.y * DROP_PANEL_HEIGHT_PERCENT);

		dropPanel = new Panel(screen, dropPanelPosition, dropPanelSize);
		dropPanel.setIsDragDropDropElement(true);
		dropPanel.setIsMovable(false);
		dropPanel.setIsResizable(false);
		dropPanel.setIgnoreGlobalAlpha(false);
		dropPanel.setGlobalAlpha(0.6f);
		dropPanel.setTextAlign(BitmapFont.Align.Center);
		dropPanel.setTextVAlign(BitmapFont.VAlign.Center);
		dropPanel.setText("Drop to cast");
	}

	public void initialize(AppStateManager stateManager, Application app) {
		if (initialized)
			return;

		super.initialize(stateManager, app);

		selectionArea = new CardSelectionArea<SpellCardElement>(screen, new Vector2f(), panelSize);
		panel.addChild(selectionArea);

		screen.addElement(panel);
		screen.addElement(dropPanel);

		panel.setIsVisible(panel.getIsVisible());
	}

	public void stateAttached(AppStateManager stateManager){
		if (!initialized)
			initialize(stateManager, stateManager.getApplication());
		updateFromDeck();
	}

	private void updateFromDeck(){
		List<SpellCardItem> cards = Program.getInstance().getMainInventory().filter(SpellCardItem.class, new TypeFilter(ItemTypes.SPELL_CARD));
		for (SpellCardItem card: cards){
			SpellCardElement element = new SpellCardElement(screen, new Vector2f(), panelSize.y * 0.9f, card);
			element.setMouseButtonDispatcher(new SpellCardClickDispatcher(screen, element));
			selectionArea.addCard(element);
		}
	}

	public void removeCard(SpellCardItem card){
		SpellCardElement found = null;
		for (SpellCardElement element: selectionArea.getCards())
			if (element.getCard() == card){
				found = element;
				break;
			}

		if (found != null)
			selectionArea.removeCard(found);
	}

	public void cleanup() {
		super.cleanup();

		selectionArea.clear();
		screen.removeElement(panel);
		screen.removeElement(dropPanel);
	}

	public Panel getPanel() {
		return panel;
	}

	public Panel getDropPanel() {
		return dropPanel;
	}

	public void update(float tpf){
		BattleScene battleScene = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class);
		CardMaster master = Program.getInstance().getMainPlayer();
		Unit usedUnit = master.getUsedUnit();
		boolean turning = master.getCurrentBoard().getCurrentTurningPlayer() == master;
		boolean used = usedUnit == null;
		boolean state = master.getCurrentBoard().getState() == Board.STATE_WAIT_FOR_ORDER;
		boolean casting = battleScene.getBattleState().getSpellToCast() == null;

		boolean canCast = turning && used && state && casting;
		boolean cardBeingDragged = false;

		if (screen.getDragElement() instanceof SpellCardElement){
			SpellCardElement dragged = (SpellCardElement) screen.getDragElement();
			cardBeingDragged = dragged.isBeingDragged();
		}

		dropPanel.setIsVisible(canCast && cardBeingDragged);

		for (SpellCardElement element: selectionArea.getCards()){
			element.setEnabled(canCast);

			if (element.getHasFocus()){
				battleScene.setFocusedSpellCard(element.getCard());
				return;
			}
		}
		battleScene.setFocusedSpellCard(null);
	}
}
