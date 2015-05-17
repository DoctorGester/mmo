package core.ui.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import core.board.*;
import core.graphics.MainFrame;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.ui.BattleState;
import core.ui.ChatUIState;
import core.ui.MainChatBox;
import core.ui.UI;
import gui.core.V;
import program.main.Program;
import program.main.SceneUtil;
import shared.board.*;
import shared.board.data.AbilityData;
import shared.board.data.UnitData;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class BattleUIState extends AbstractAppState {
	private static final float PANEL_HEIGHT_PERCENT = 0.25f,
							   CENTER_PANEL_HEIGHT_PERCENT = 0.2f,
							   ABILITY_ICON_SIZE_PERCENT = 0.4f,
							   ABILITY_SECTION_START_PERCENT = 0.45f,
							   BUFF_ICON_SIZE_PERCENT = 0.12f,
							   PASSIVE_ICON_SIZE_PERCENT = 0.16f,
							   ICON_SIDE_SIZE_PERCENT = 0.2f,
							   ICON_SPACE_FOR_DATA_PERCENT = 0.1f,
							   ICON_FONT_SIZE_PERCENT = 0.18f,
							   STANDARD_OFFSET = 0.05f,
							   BETWEEN_OFFSET = 0.02f,
							   STAT_ROW_HEIGHT = 0.2f,
							   BUTTON_WIDTH = 0.12f,
							   BUTTON_HEIGHT = 0.06f,
							   TIME_LABEL_SIZE_PERCENT = 0.4f,
							   FLAG_RADIUS = 0.25f;

	private static final int ABILITIES_IN_A_ROW = 2;
	private static final int ABILITIES_ROWS = 2;

	private MainFrame frame;
	private Screen screen;

	private Vector2f dimension;

	private Panel rightPanel, centerPanel;
	private Element description;

	private BattleState battleState;

	private Map<String, AbilityButton> buttonCache = new HashMap<String, AbilityButton>();
	private Map<Element, String> descriptions = new HashMap<Element, String>();
	private List<BuffIcon> buffIcons = new ArrayList<BuffIcon>();

	// TODO remove move stuff
	private Icon moveIcon, attackIcon, healthIcon, restIcon;
	private Label nameLabel, moveLabel, attackLabel, healthLabel, restLabel;
	private Label turningPlayer, turnTime;
	private Button menuButton, skipButton, bookButton, chatButton;

	private Vector2f rightPanelSize, centerPanelSize;

	private ClientUnit lastUpdated = null;
	private UnitData lastUnitData = null;

	private BannerElement banners[];

	public BattleUIState(MainFrame frame) {
		this.frame = frame;
		this.screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public void setBattleState(BattleState battleState) {
		this.battleState = battleState;
	}

	public BattleState getBattleState() {
		return battleState;
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		// Right
		rightPanelSize = V.f(dimension.x * 0.3f, dimension.y * PANEL_HEIGHT_PERCENT);
		Vector2f panelPosition = V.f(dimension.x * 0.7f, dimension.y * (1f - PANEL_HEIGHT_PERCENT));

		rightPanel = new Panel(screen, panelPosition, rightPanelSize);
		rightPanel.setIgnoreMouse(true);

		screen.addElement(rightPanel);

		// Center
		centerPanelSize = V.f(dimension.x * 0.16f, dimension.y * CENTER_PANEL_HEIGHT_PERCENT);
		panelPosition = V.f(dimension.x * 0.42f, dimension.y * (1f - CENTER_PANEL_HEIGHT_PERCENT));

		centerPanel = new Panel(screen, panelPosition, centerPanelSize);
		centerPanel.setIgnoreMouse(true);

		turningPlayer = new Label(screen, V.f(0, centerPanelSize.y * 0.8f),
										  V.f(centerPanelSize.x, centerPanelSize.y * 0.2f));
		turningPlayer.setInitialized();
		turningPlayer.setTextAlign(BitmapFont.Align.Center);
		turningPlayer.setTextVAlign(BitmapFont.VAlign.Center);

		turnTime = new Label(screen, Vector2f.ZERO, centerPanelSize.mult(TIME_LABEL_SIZE_PERCENT));
		turnTime.setFontSize(centerPanelSize.y * TIME_LABEL_SIZE_PERCENT / 1.5f);
		turnTime.setTextVAlign(BitmapFont.VAlign.Center);
		turnTime.setTextAlign(BitmapFont.Align.Center);

		centerPanel.addChild(turningPlayer);
		centerPanel.addChild(turnTime);

		turnTime.centerToParent();
		turnTime.setInitialized();

		screen.addElement(centerPanel);

		turnTime.setZOrder(centerPanel.getZOrder() + centerPanelSize.x * FLAG_RADIUS * 2f + 1);

		Vector2f leftPanelSize = V.f(dimension.x * 0.3f, dimension.y * PANEL_HEIGHT_PERCENT);

		frame.addUIState(UI.STATE_CHAT);

		MainChatBox chatBox = frame.getUIState(UI.STATE_CHAT, ChatUIState.class).getChatBox();
		chatBox.setPosition(Vector2f.ZERO);
		chatBox.setDimensions(leftPanelSize);
		chatBox.setIsVisible(true);

		frame.addUIState(UI.STATE_BATTLE_LOG);
		frame.addUIState(UI.STATE_SPELL_SELECTOR);

		frame.getUIState(UI.STATE_BATTLE_LOG, BattleLogUIState.class).getLog().setIsVisible(false);
		frame.getUIState(UI.STATE_SPELL_SELECTOR, SpellSelectorUIState.class).getPanel().setIsVisible(false);

		float side = rightPanelSize.y * ICON_SIDE_SIZE_PERCENT;
		Vector2f iconSize = new Vector2f(side, side);
		Vector2f labelSize = new Vector2f(rightPanelSize.x * ICON_SPACE_FOR_DATA_PERCENT, side);
		Vector2f bigLabelSize = new Vector2f(labelSize.x * 2.5f, labelSize.y);

		moveIcon = new Icon(screen, new Vector2f(), iconSize, "res/textures/interface/move.png");
		attackIcon = new Icon(screen, new Vector2f(), iconSize, "res/textures/interface/attack.png");
		healthIcon = new Icon(screen, new Vector2f(), iconSize, "res/textures/interface/health.png");
		restIcon = new Icon(screen, new Vector2f(), iconSize, "res/textures/interface/rest.png");

		nameLabel = new Label(screen, new Vector2f(), bigLabelSize.add(new Vector2f(bigLabelSize.x, 0)));
		moveLabel = new Label(screen, new Vector2f(), labelSize);
		attackLabel = new Label(screen, new Vector2f(), labelSize);
		healthLabel = new Label(screen, new Vector2f(), bigLabelSize);
		restLabel = new Label(screen, new Vector2f(), bigLabelSize);

		nameLabel.setInitialized();
		moveLabel.setInitialized();
		attackLabel.setInitialized();
		healthLabel.setInitialized();
		restLabel.setInitialized();

		float textSize = rightPanelSize.y * ICON_FONT_SIZE_PERCENT;

		nameLabel.setFontSize(textSize / 1.3f);
		moveLabel.setFontSize(textSize);
		attackLabel.setFontSize(textSize);
		healthLabel.setFontSize(textSize);
		restLabel.setFontSize(textSize);

		description = createDescription();
		screen.addElement(description);

		createButtons();
		updateFromBoard();
	}

	private AbilityButton getAbilityButton(AbilityData abilityData){
		String icon = abilityData.getIcon();
		AbilityButton button = buttonCache.get(icon);
		if (button == null){
			float side = rightPanelSize.y * ABILITY_ICON_SIZE_PERCENT;
			Vector2f size = new Vector2f(side, side);
			button = new AbilityButton(screen, new Vector2f(0, 0), size, icon);
			button.setInitialized();
			buttonCache.put(icon, button);

			addDescriptionForElement(button, abilityData.getDescription());
		}

		return button;
	}

	public void updateFromBoard(){
		Board board = battleState.getBoard();
		turningPlayer.setText(board.getCurrentTurningPlayer().getName());
		turningPlayer.setFontColor(ClientBoard.PLAYER_COLORS[board.getCurrentTurningPlayer().getBattleId()]);

		if (board.getState() == Board.STATE_WAIT_FOR_PICK)
			skipButton.setText("Random");
		else if (board.getState() == Board.STATE_WAIT_FOR_PLACEMENT)
			skipButton.setText("Finish");
		else
			skipButton.setText("End turn");
		if (board.getState() != Board.STATE_WAIT_FOR_PLACEMENT)
			skipButton.setIsEnabled(board.getCurrentTurningPlayer() == Program.getInstance().getMainPlayer());
		else
			skipButton.setIsEnabled(true);

		bookButton.setIsEnabled(board.getState() != Board.STATE_WAIT_FOR_PICK);

		if (banners == null){
			banners = new BannerElement[board.getCardMasters().size()];

			Vector2f size = V.f(centerPanelSize.x * 0.2f, centerPanelSize.y * 0.7f);
			for(int i = 0; i < banners.length; i++){
				banners[i] = new BannerElement(screen, new Vector2f(0, centerPanelSize.y * 0.2f), size, ClientBoard.PLAYER_COLORS[i], "");

				centerPanel.addChild(banners[i]);
			}
		}
	}

	public void updateUnitUI(ClientUnit unit){
		rightPanel.removeAllChildren();
		for (BuffIcon buffIcon: buffIcons){
			removeDescriptionForElement(buffIcon);
			screen.removeElement(buffIcon);
		}

		buffIcons.clear();

		if (unit == null){
			lastUpdated = null;
			lastUnitData = null;
			return;
		}

		lastUnitData = unit.getUnitData();

		float side = rightPanelSize.y * ABILITY_ICON_SIZE_PERCENT;
		float xOffset = ((rightPanelSize.x * (1f - ABILITY_SECTION_START_PERCENT)) - ABILITIES_IN_A_ROW * side * 1.1f) / 2f;
		float yOffset = (rightPanelSize.y - side * 1.1f * ABILITIES_ROWS) / 2f;

		int xIndex = 0;
		int yIndex = 0;
		for (Ability ability : unit.getAbilities()){
			float yPos = rightPanelSize.y - (side * 1.1f * yIndex + side + yOffset);
			float xPos = rightPanelSize.x * ABILITY_SECTION_START_PERCENT + xOffset + side * 1.1f * xIndex;
			AbilityButton button = getAbilityButton(ability.getAbilityData());
			button.setPosition(xPos, yPos);
			button.setUserData(AbilityButton.SPELL_KEY, yIndex * ABILITIES_IN_A_ROW + xIndex);

			int cd = ability.getAbilityData().getCoolDown();
			int remaining = ability.getCoolDownLeft();

			float progress = (float) remaining / cd;

			if (lastUpdated == unit)
				button.setCoolDownProgress(progress);
			else
				button.setCoolDownProgressImmediate(progress);

			button.setTurnsLeft(remaining);

			Unit usedUnit = unit.getOwner().getUsedUnit();
			boolean owned = Program.getInstance().getMainPlayer() == unit.getOwner();
			boolean turning = unit.getBoard().getCurrentTurningPlayer() == unit.getOwner();
			boolean used = usedUnit == unit || usedUnit == null;
			boolean state = unit.getBoard().getState() == Board.STATE_WAIT_FOR_ORDER;
			boolean casting = battleState.getSpellToCast() == ability;
			boolean wait = unit.getState() == Unit.STATE_WAIT;
			boolean disabled = unit.controlApplied(ControlType.STUN) || unit.controlApplied(ControlType.SILENCE);

			button.setActive(owned && turning && used && state && wait && !disabled);
			button.setGlowing(casting);

			rightPanel.addChild(button);

			xIndex++;

			if (xIndex == ABILITIES_IN_A_ROW){
				xIndex = 0;
				yIndex++;
			}
		}

		lastUpdated = unit;

		updateBuffIcons(unit);
		updateElements();

		nameLabel.setText(unit.getUnitData().getName());
		healthLabel.setText(String.valueOf(unit.getCurrentHealth()) + "/" + String.valueOf(unit.getMaxHealth()));
		attackLabel.setText(String.valueOf(unit.getTotalAttackDamage()));
		restLabel.setText((unit.getRestTime() - unit.getRestLeft()) + "/" + unit.getRestTime());
		moveLabel.setText(String.valueOf(unit.getMaxActionPoints() - 1));
	}

	public void updateUnitUI(UnitData unitData){
		lastUnitData = unitData;

		rightPanel.removeAllChildren();

		if (unitData == null)
			return;

		float side = rightPanelSize.y * ABILITY_ICON_SIZE_PERCENT;
		float xOffset = ((rightPanelSize.x * (1f - ABILITY_SECTION_START_PERCENT)) - ABILITIES_IN_A_ROW * side * 1.1f) / 2f;
		float yOffset = (rightPanelSize.y - side * 1.1f * ABILITIES_ROWS) / 2f;

		int xIndex = 0;
		int yIndex = 0;
		for (String id: unitData.getSpells()){
			float yPos = rightPanelSize.y - (side * 1.1f * yIndex + side + yOffset);
			float xPos = rightPanelSize.x * ABILITY_SECTION_START_PERCENT + xOffset + side * 1.1f * xIndex;
			AbilityButton button = getAbilityButton(Program.getInstance().getSpellDataById(id));
			button.setPosition(xPos, yPos);
			button.setCoolDownProgress(1f);

			rightPanel.addChild(button);
			xIndex++;

			if (xIndex == ABILITIES_IN_A_ROW){
				xIndex = 0;
				yIndex++;
			}
		}

		updateElements();

		nameLabel.setText(unitData.getName());
		healthLabel.setText(String.valueOf(unitData.getHealth()));
		attackLabel.setText(String.valueOf(unitData.getDamage()));
		restLabel.setText(String.valueOf(unitData.getRestTime()));
		moveLabel.setText(String.valueOf(unitData.getActionPoints() - 1));
	}

	private void updateBuffIcons(ClientUnit unit){
		float offset = rightPanelSize.x * STANDARD_OFFSET;
		Vector2f size = new Vector2f(rightPanelSize.x * BUFF_ICON_SIZE_PERCENT, rightPanelSize.x * BUFF_ICON_SIZE_PERCENT);
		int index = 0;

		for (ClientBuff buff: unit.getBuffs()){
			float xOffset = offset + index * size.x + index * (size.x * STANDARD_OFFSET * 2);
			Vector2f position = rightPanel.getPosition().add(new Vector2f(xOffset, rightPanelSize.y + offset));
			BuffIcon icon = new BuffIcon(screen, position, size, buff.getBuffData().getIcon());
			icon.setInitialized();
			icon.setBuffData(buff);

			addDescriptionForElement(icon, buff.getBuffData().getDescription());

			screen.addElement(icon);
			buffIcons.add(icon);
			index++;
		}
	}

	private void updateElements(){
		float betweenOffsetX = rightPanelSize.x * BETWEEN_OFFSET;
		float betweenOffsetY = rightPanelSize.y * BETWEEN_OFFSET;
		float yOffset = rightPanelSize.y * STANDARD_OFFSET;
		float offset = rightPanelSize.x * STANDARD_OFFSET;

		// Null element declares row's end
		Element stats[] = {
			nameLabel, null,
			healthIcon, healthLabel, null,
			attackIcon, attackLabel, null,
			restIcon, restLabel
		};

		for(Element element: stats){
			if (element == null){
				offset = rightPanelSize.x * STANDARD_OFFSET;
				yOffset += betweenOffsetY + rightPanelSize.y * STAT_ROW_HEIGHT;
				continue;
			}

			element.setPosition(offset, rightPanelSize.y - yOffset - element.getHeight());

			rightPanel.addChild(element);

			offset += betweenOffsetX + element.getWidth();
		}
	}

	private static float interpolate(float from, float to, float step) {
		if (from > to)
			to = FastMath.TWO_PI + to;

		return FastMath.interpolateLinear(step, from, to) % FastMath.TWO_PI;
	}

	public void update(float tpf){
		ClientBoard board = battleState.getBoard();
		turnTime.setText(String.valueOf((int) board.getTimeRemaining()));
		turnTime.setZOrder(centerPanel.getZOrder() + centerPanelSize.x * FLAG_RADIUS * 2f + 1);

		if (banners != null){
			int turning = board.getCardMasters().indexOf(board.getCurrentTurningPlayer());
			float radius = centerPanelSize.x  * FLAG_RADIUS;
			float offset = centerPanelSize.x / 2f - centerPanelSize.x * 0.1f;

			for(int i = 0; i < banners.length; i++){
				float piece = FastMath.TWO_PI / banners.length;
				float target = piece * (turning - i) + piece / 2f;

				BannerElement banner = banners[i];

				banner.setAngle(interpolate(banner.getAngle(), target, tpf));

				float x = offset + FastMath.cos(banner.getAngle()) * radius,
					  z = FastMath.sin(banner.getAngle()) * radius;

				banner.setX(x);
				banner.setZOrder(centerPanel.getZOrder() + radius + z);
			}
		}

		description.setIsVisible(false);

		for (Element element: descriptions.keySet())
			if (element.getHasFocus()){
				updateDescription(descriptions.get(element));
				description.setIsVisible(true);
				break;
			}

		if (lastUpdated == null)
			return;

		ClientAbility focused = null;
		for (ClientAbility spell: lastUpdated.getAbilities()){
			AbilityButton button = getAbilityButton(spell.getAbilityData());
			button.updateCooldownProgress();
			if (button.getHasFocus())
				focused = spell;
		}
		SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).setFocusedSpell(focused);
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(centerPanel);
		screen.removeElement(rightPanel);

		screen.removeElement(bookButton);
		screen.removeElement(chatButton);
		screen.removeElement(menuButton);
		screen.removeElement(skipButton);

		banners = null;

		frame.removeUIState(UI.STATE_CHAT);
		frame.removeUIState(UI.STATE_BATTLE_LOG);
		frame.removeUIState(UI.STATE_SPELL_SELECTOR);

		for (BuffIcon icon: buffIcons)
			icon.removeFromParent();

		buffIcons.clear();

		// Might want to reconsider clearing caches, as long as spells do not change through game
		buttonCache.clear();
		descriptions.clear();
	}

	public void addDescriptionForElement(Element element, String text){
		descriptions.put(element, text);
	}

	public void removeDescriptionForElement(Element element){
		descriptions.remove(element);
	}

	private void updateDescription(String text){
		description.setText(text);
		float height = description.getTextElement().getLineCount() * description.getTextElement().getLineHeight();
		description.setHeight(height + 24f);
	}

	private Element createDescription(){
		Vector2f size = new Vector2f(dimension.x * 0.3f, dimension.y * 0.05f);
		Vector2f position = new Vector2f(0, dimension.y * 0.3f);

		Element element = new Panel(screen, position, size);

		element.setInitialized();
		element.setZOrder(Float.MAX_VALUE);
		element.setTextPadding(12f);
		return element;
	}

	private void createButtons(){
		Vector2f buttonSize = new Vector2f(BUTTON_WIDTH * dimension.x, BUTTON_HEIGHT * dimension.y);
		menuButton = new ButtonAdapter(screen, new Vector2f(dimension.x * 0.3f, buttonSize.y), buttonSize);
		chatButton = new ButtonAdapter(screen, new Vector2f(dimension.x * 0.3f, 0), buttonSize){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				MainChatBox chatBox = SceneUtil.getUI(UI.STATE_CHAT, ChatUIState.class).getChatBox();
				BattleLog log = SceneUtil.getUI(UI.STATE_BATTLE_LOG, BattleLogUIState.class).getLog();

				if (chatBox.getIsVisible()){
					chatBox.setIsVisible(false);
					log.setIsVisible(true);
					chatButton.setText("Chat");
				} else {
					chatBox.setIsVisible(true);
					log.setIsVisible(false);
					chatButton.setText("Log");
				}
			}
		};
		skipButton = new ButtonAdapter(screen, new Vector2f(dimension.x * (0.46f + BUTTON_WIDTH), buttonSize.y), buttonSize){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				if (battleState.getBoard().getState() == Board.STATE_WAIT_FOR_PLACEMENT)
					Program.getInstance().getBattleController().battlePlacementFinishedLocal(battleState);
				else
					Program.getInstance().getBattleController().battleSkipTurn();
			}
		};
		bookButton = new ButtonAdapter(screen, new Vector2f(dimension.x * (0.46f + BUTTON_WIDTH), 0), buttonSize){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				SceneUtil.getUI(UI.STATE_SPELL_SELECTOR, SpellSelectorUIState.class).getPanel().setIsVisible();
			}
		};

		menuButton.setIsEnabled(false);
		bookButton.setIsEnabled(false);

		menuButton.setInitialized();
		skipButton.setInitialized();
		chatButton.setInitialized();
		bookButton.setInitialized();

		menuButton.setText("Menu");
		chatButton.setText("Log");
		bookButton.setText("Spells");

		screen.addElement(menuButton);
		screen.addElement(skipButton);
		screen.addElement(chatButton);
		screen.addElement(bookButton);
	}
}
