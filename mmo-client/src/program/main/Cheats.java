package program.main;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import core.board.ClientBoard;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.ui.BattleState;

/**
 * @author doc
 */
public class Cheats {
	private Program program;

	public Cheats(Program program){
		this.program = program;
	}

	public void init(){
		InputManager inputManager = program.getMainFrame().getInputManager();
		inputManager.addMapping("enter_battle", new KeyTrigger(KeyInput.KEY_B));
		inputManager.addListener(new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				ClientBoard board = new ClientBoard(8, 8);
				BattleState state = new BattleState();
				state.setBoard(board);

				SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).setBattleState(state);

				program.getMainFrame().setScene(Scenes.BATTLE);
				program.getMainFrame().setUIState(null);
			}
		}, "enter_battle");
	}
}
