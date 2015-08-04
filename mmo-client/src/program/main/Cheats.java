package program.main;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.scene.Node;
import core.board.ClientBoard;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.ui.BattleState;
import core.ui.deck.DeckControl;
import shared.items.Item;
import shared.items.types.UnitCardItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author doc
 */
public class Cheats {
	private Program program;
	private DeckControl deckControl;

	public Cheats(Program program) {
		this.program = program;
	}

	public void init() {
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

		inputManager.addMapping("card_control", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addListener(new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				if (!isPressed)
					return;

				List<Item> deck = new ArrayList<Item>();

				for (int i = 0; i < 30; i++){
					UnitCardItem item = new UnitCardItem();
					List<String> idList = new ArrayList<String>(Program.getInstance().unitDataMap.keySet());
					Collections.shuffle(idList);
					item.setUnitId(idList.get(0));

					deck.add(item);
				}

				Node root = program.getMainFrame().getRootNode();
				deckControl = new DeckControl(program.getMainFrame());
				deckControl.setDeck(deck);
				deckControl.enableCardHover();
				deckControl.enableCardDrag();
				root.addControl(deckControl);
			}
		}, "card_control");

		inputManager.addMapping("+wheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addMapping("-wheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addListener(new AnalogListener() {
			@Override
			public void onAnalog(String name, float value, float tpf) {
				if (deckControl != null) {
					if (name.startsWith("+"))
						deckControl.spin(value * tpf);
					else
						deckControl.spin(-value * tpf);
				}
			}
		}, "+wheel", "-wheel");
	}
}
