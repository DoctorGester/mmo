package core.ui.map;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.ui.ChatUIState;
import core.ui.MainChatBox;
import core.ui.MapController;
import core.ui.UI;
import program.main.Program;
import program.main.SceneUtil;
import shared.map.CardMaster;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.menuing.Menu;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author doc
 */
public class MapUIState extends AbstractAppState{
	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	private List<Button> bottomBarButtons = new ArrayList<Button>();

	private Menu contextMenu;
	private CardMaster menuTarget;

	private static final String PROFILE_CAPTION = "Profile - %s";
	private static final float BUTTON_SIZE = 0.08f;

	private boolean leftOccupied = false;

	private Map<String, MenuItemListener> menuItemListenerMap = new HashMap<String, MenuItemListener>();

	public MapUIState(MainFrame frame) {
		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		createBottomBar();

		contextMenu = new Menu(screen, Vector2f.ZERO, false) {
			@Override
			public void onMenuItemClicked(int index, Object value, boolean isToggled) {
				String action = value.toString();
				MenuItemListener listener = menuItemListenerMap.get(action);

				if (listener != null)
					listener.itemSelected(menuTarget);
			}
		};

		screen.addElement(contextMenu);

		frame.addUIState(UI.STATE_REQUESTS);
		frame.addUIState(UI.STATE_CHAT);

		frame.getUIState(UI.STATE_CHAT, ChatUIState.class).getChatBox().setIsVisible(false);
		frame.getUIState(UI.STATE_CHAT, ChatUIState.class).getChatBox().setDimensions(dimension.mult(0.3f));
	}

	private void createBottomBar() {
		addBottomBarButton("res/textures/interface/pawn.png", 0, new BottomBarButtonListener() {
			@Override
			public void buttonPressed() {
				if (frame.hasUIState(UI.STATE_PROFILE)) {
					frame.removeUIState(UI.STATE_PROFILE);
				} else {
					frame.addUIState(UI.STATE_PROFILE);
					frame.removeUIState(UI.STATE_CARDBOOK);

					MapController controller = Program.getInstance().getMapController();
					CardMaster main = Program.getInstance().getMainPlayer();

					controller.requestProfileInfo(Program.getInstance().getMainPlayer());
					SceneUtil.getUI(UI.STATE_PROFILE, CardMasterProfileUIState.class).getProfileInfoPanel().setTarget(main);
					SceneUtil.getUI(UI.STATE_PROFILE, CardMasterProfileUIState.class).setInventoryLocked(false);
				}

				updateLeftSide();
			}
		});

		addBottomBarButton("res/textures/interface/duel.png", 1, new BottomBarButtonListener() {
			@Override
			public void buttonPressed() {
				RequestsUIState ui = SceneUtil.getUI(UI.STATE_REQUESTS, RequestsUIState.class);
				ui.showPanel(!ui.isPanelVisible());

				updateLeftSide();
			}
		});

		addBottomBarButton("res/textures/interface/chat.png", 2, new BottomBarButtonListener() {
			@Override
			public void buttonPressed() {
				MainChatBox chatBox = SceneUtil.getUI(UI.STATE_CHAT, ChatUIState.class).getChatBox();
				chatBox.setIsVisible(!chatBox.getIsVisible());

				updateLeftSide();
			}
		});
	}

	public void addBottomBarButton(String image, int position, final BottomBarButtonListener listener){
		Vector2f buttonSize = new Vector2f(BUTTON_SIZE * dimension.y, BUTTON_SIZE * dimension.y);
		float buttonsY = dimension.y * 0.97f - buttonSize.y;

		Vector2f buttonPosition = new Vector2f(dimension.x * ((BUTTON_SIZE + 0.015f) * position + 0.03f), buttonsY);
		Button button = new ButtonAdapter(screen, buttonPosition, buttonSize){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				listener.buttonPressed();
			}
		};
		button.setButtonIcon(buttonSize.x * 0.7f, buttonSize.y * 0.7f, image);
		screen.addElement(button);
	}

	public void addMenuItem(String id, String text, MenuItemListener listener){
		contextMenu.addMenuItem(text, id, null);
		menuItemListenerMap.put(id, listener);
	}

	public void updateLeftSide(){
		MainChatBox chatBox = SceneUtil.getUI(UI.STATE_CHAT, ChatUIState.class).getChatBox();

		leftOccupied = (chatBox != null && chatBox.getIsVisible()) ||
					   SceneUtil.getUI(UI.STATE_REQUESTS, RequestsUIState.class).isPanelVisible();

		Element elementsAtLeftSide[] = new Element[]{
				chatBox,
				SceneUtil.getUI(UI.STATE_REQUESTS, RequestsUIState.class).getPanel()
		};

		float y = dimension.y * (0.03f + BUTTON_SIZE);

		for(Element element: elementsAtLeftSide){
			if (element == null)
				continue;
			if (element.getIsVisible()){
				y += dimension.y * 0.03f;
				element.setPosition(screen.getWidth() * 0.03f, y);
				y += element.getHeight();
			}
		}

		if (leftOccupied){
			frame.getUIState(UI.STATE_CARDBOOK, CardbookUIState.class).slideRight();
			frame.getUIState(UI.STATE_PROFILE, CardMasterProfileUIState.class).slideRight();
		} else {
			frame.getUIState(UI.STATE_CARDBOOK, CardbookUIState.class).slideCenter();
			frame.getUIState(UI.STATE_PROFILE, CardMasterProfileUIState.class).slideCenter();
		}
	}

	public void showMenu(CardMaster master, Vector2f position){
		menuTarget = master;
		contextMenu.removeAllMenuItems();


		addMenuItem("profile", String.format(PROFILE_CAPTION, master.getName()), new MenuItemListener() {
			@Override
			public void itemSelected(CardMaster menuTarget) {
				MapController controller = Program.getInstance().getMapController();
				controller.requestProfileInfo(menuTarget);
				CardMasterProfileUIState profileState = SceneUtil.getUI(UI.STATE_PROFILE, CardMasterProfileUIState.class);

				profileState.getProfileInfoPanel().setTarget(menuTarget);
				profileState.setInventoryLocked(menuTarget != Program.getInstance().getMainPlayer());

				frame.addUIState(UI.STATE_PROFILE);

				updateLeftSide();
			}
		});

		addMenuItem("attack", "Attack", new MenuItemListener() {
			@Override
			public void itemSelected(CardMaster menuTarget) {
				MapController controller = Program.getInstance().getMapController();
				controller.requestAttackPlayer(menuTarget);
			}
		});
		if (master.getType() == CardMaster.TYPE_PLAYER) {

			addMenuItem("duel", "Duel", new MenuItemListener() {
				@Override
				public void itemSelected(CardMaster menuTarget) {
					MapController controller = Program.getInstance().getMapController();
					controller.requestDuelPlayer(Program.getInstance().getVisiblePlayerById(menuTarget.getId()));
				}
			});

			addMenuItem("trade", "Trade", new MenuItemListener() {
				@Override
				public void itemSelected(CardMaster menuTarget) {
					MapController controller = Program.getInstance().getMapController();
					controller.requestTradePlayer(Program.getInstance().getVisiblePlayerById(menuTarget.getId()));
				}
			});

		}

		contextMenu.showMenu(null, position.x, position.y - contextMenu.getHeight());
	}

	public void hideMenu(){
		contextMenu.hideMenu();
	}

	public void cleanup() {
		super.cleanup();

		for (Button button: bottomBarButtons)
			screen.removeElement(button);

		frame.removeUIState(UI.STATE_CHAT);
		frame.removeUIState(UI.STATE_REQUESTS);
	}

	private static interface BottomBarButtonListener{
		public void buttonPressed();
	}

	private static interface MenuItemListener{
		public void itemSelected(CardMaster menuTarget);
	}
}
