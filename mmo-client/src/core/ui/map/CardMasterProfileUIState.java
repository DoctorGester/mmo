package core.ui.map;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import core.graphics.MainFrame;
import core.ui.CloseablePanel;
import core.ui.UI;
import core.ui.inventory.InventoryPanel;
import program.main.Program;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.core.utils.UIDUtil;

/**
 * @author doc
 */
public class CardMasterProfileUIState extends AbstractAppState {
	private static final float PANEL_WIDTH = 0.6f,
							   PANEL_HEIGHT = 0.74f,
							   Y_POSITION = 0.14f;

	private MainFrame frame;
	private Screen screen;
	private Vector2f dimension;

	private CloseablePanel mainPanel;
	private Vector2f panelSize;

	private ProfileInfoPanel profileInfoPanel;
	private InventoryPanel inventoryPanel;
	private Element inventoryLock;

	public CardMasterProfileUIState(MainFrame frame) {
		this.frame = frame;
		screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
		panelSize = new Vector2f(dimension.x * PANEL_WIDTH, dimension.y * PANEL_HEIGHT);
		mainPanel = new CloseablePanel(screen, Vector2f.ZERO, panelSize){
			public void onClose() {
				CardMasterProfileUIState.this.frame.removeUIState(UI.STATE_PROFILE);
			}
		};
		inventoryPanel = new InventoryPanel(screen);
		inventoryPanel.setX(panelSize.x - inventoryPanel.getWidth());
		inventoryPanel.setInventory(Program.getInstance().getMainInventory());

		profileInfoPanel = new ProfileInfoPanel(screen);

		inventoryLock = new Element(screen, UIDUtil.getUID(), inventoryPanel.getPosition(), inventoryPanel.getDimensions(), Vector4f.ZERO, "res/textures/lock.png");
		inventoryLock.setIgnoreMouse(true);

		setInventoryLocked(false);

		mainPanel.addChild(inventoryPanel);
		mainPanel.addChild(profileInfoPanel);
		mainPanel.addChild(inventoryLock);
		mainPanel.setIgnoreMouse(true);
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		screen.addElement(mainPanel);

		if (mainPanel.getPosition().equals(Vector2f.ZERO)){
			mainPanel.centerToParent();
			mainPanel.setInitialized();
			mainPanel.setY(dimension.y * Y_POSITION);
		}
	}

	public void setInventoryLocked(boolean locked){
		inventoryPanel.setIsVisible(!locked);
		inventoryLock.setIsVisible(locked);
	}

	public ProfileInfoPanel getProfileInfoPanel() {
		return profileInfoPanel;
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(mainPanel);
	}

	public void slideRight(){
		mainPanel.setInitialized();
		mainPanel.setPosition(dimension.x * 0.97f - panelSize.x, dimension.y * Y_POSITION);
	}

	public void slideCenter(){
		mainPanel.centerToParent();
		mainPanel.setInitialized();
		mainPanel.setY(dimension.y * Y_POSITION);
	}
}