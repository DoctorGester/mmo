package core.ui.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import core.graphics.MainFrame;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import gui.core.V;
import program.main.Program;
import program.main.SceneUtil;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.core.Screen;

/**
 * @author doc
 */
public class BattlePlacementUIState extends AbstractAppState {
	private static final float FINISH_PLACEMENT_BUTTON_WIDTH = 0.1f;

	private MainFrame frame;
	private Screen screen;

	private Vector2f dimension;

	private Button finishButton;

	public BattlePlacementUIState(MainFrame frame) {
		this.frame = frame;
		this.screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		Vector2f buttonPosition = V.f(0, dimension.y * 0.25f),
				 buttonSize = V.f(dimension.x * 0.3f, dimension.y * 0.1f);
		finishButton = new ButtonAdapter(screen, buttonPosition, buttonSize){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				BattleScene battleScene = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class);
				Program.getInstance().getBattleController().battlePlacementFinishedLocal(battleScene.getBattleState());
			}
		};
		finishButton.setText("Finish placement");
		finishButton.setInitialized();

		screen.addElement(finishButton);
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(finishButton);
	}
}
