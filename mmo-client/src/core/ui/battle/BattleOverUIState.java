package core.ui.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.main.CardMaster;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;

/**
 * @author doc
 */
public class BattleOverUIState extends AbstractAppState {
	private static final float FINAL_PANEL_WIDTH = 0.8f,
							   FINAL_PANEL_HEIGHT = 0.4f,
							   EXIT_BUTTON_WIDTH = 0.3f,
							   EXIT_BUTTON_HEIGHT = 0.15f,
							   STATUS_HEIGHT = 0.15f;


	public static final int STATUS_VICTORY = 0x00,
							STATUS_DEFEAT = 0x01,
							STATUS_DRAW = 0x02;

	private static final String STATUSES[] = {
		"VICTORY",
		"DEFEAT",
		"DRAW"
	};

	private static final ColorRGBA STATUS_COLORS[] = {
			ColorRGBA.Red,
			ColorRGBA.Yellow,
			ColorRGBA.Green
	};

	private MainFrame frame;
	private Screen screen;

	private Vector2f dimension;

	private Panel panel;
	private Label status;

	private boolean exitClicked;

	public BattleOverUIState(MainFrame frame) {
		this.frame = frame;
		this.screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
		status = new Label(screen, "", new Vector2f(), new Vector2f());
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		Vector2f panelSize = new Vector2f(dimension.x * FINAL_PANEL_WIDTH, dimension.y * FINAL_PANEL_HEIGHT);

		panel = new Panel(screen, Vector2f.ZERO, panelSize);
		panel.setIgnoreMouse(true);
		panel.centerToParent();

		Vector2f buttonPosition = new Vector2f(panelSize.x * ((1 - EXIT_BUTTON_WIDTH) / 2f),
											   panelSize.y * (EXIT_BUTTON_HEIGHT / 2f)),

				 buttonSize = new Vector2f(panelSize.x * EXIT_BUTTON_WIDTH, panelSize.y * EXIT_BUTTON_HEIGHT);

		Button exitButton = new ButtonAdapter(screen, buttonPosition, buttonSize) {
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				panel.setIsVisible(false);
				exitClicked = true;
			}
		};
		exitButton.setText("Exit battle");
		exitButton.setInitialized();

		Vector2f textSize = new Vector2f(panelSize.x, panelSize.y * STATUS_HEIGHT * 2);
		status.setDimensions(textSize);
		status.setFontSize(panelSize.y * STATUS_HEIGHT);
		status.setTextAlign(BitmapFont.Align.Center);
		status.setTextVAlign(BitmapFont.VAlign.Center);

		panel.addChild(exitButton);
		panel.addChild(status);

		screen.addElement(panel);
	}

	public void setStatus(int value, CardMaster ... winners){
		status.setText(STATUSES[value]);
		status.setFontColor(STATUS_COLORS[value]);
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(panel);
		exitClicked = false;
		status = new Label(screen, "", new Vector2f(), new Vector2f());
	}

	public boolean exitWasClicked(){
		return exitClicked;
	}
}
