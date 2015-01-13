package core.ui;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import core.board.*;
import core.exceptions.IncorrectHeaderException;
import core.exceptions.IncorrectPacketException;
import core.graphics.FloatingText;
import core.graphics.MainFrame;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.CardMaster;
import core.main.DataUtil;
import core.main.Packet;
import core.main.inventory.items.SpellCardItem;
import core.ui.battle.BattleLogUIState;
import core.ui.battle.BattlePickUIState;
import core.ui.battle.BattleUIState;
import program.datastore.ActualCondition;
import program.datastore.DataKey;
import program.datastore.DataStore;
import program.datastore.ExistenceCondition;
import program.main.Program;
import program.main.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class BattleController {
	private Program program;

	private Map<Integer, BattleState> battleStates = new HashMap<Integer, BattleState>();

	public BattleController(){
		this.program = Program.getInstance();
	}

	public BattleState getBattleState(int id){
		return battleStates.get(id);
	}

	public Collection<BattleState> getBattleStates(){
		return battleStates.values();
	}

	public void battlePickCard(int id){
		try {
			Packet packet = new Packet(Program.HEADER_BATTLE_PICK_ORDER, DataUtil.intToByte(id));

			program.getLocalClient().send(packet);
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void onPickStageEnd(Board board){
		Util.getScene(Scenes.BATTLE, BattleScene.class).loadFromBoard(board);
		program.getMainFrame().removeUIState(UI.STATE_BATTLE_PICK_INTERFACE);
		program.getMainFrame().addUIState(UI.STATE_BATTLE_PLACEMENT_INTERFACE);
	}

	private void logResults(TurnResults results){
		BattleLogUIState ui = Util.getUI(UI.STATE_BATTLE_LOG, BattleLogUIState.class);

		String name = results.getUnit().getUnitData().getName();
		String str = null;
		if (results.getDamageTaken() > 0){
			str = name + " takes " + results.getDamageTaken() + " damage";
			if (results.getHealDone() > 0)
				str += " and is healed for " + results.getHealDone();
		} else if (results.getHealDone() > 0){
			str = name + " is healed for " + results.getHealDone();
		}

		if (str != null)
			ui.log(str);
	}

	public void onNextTurn(final BattleState state, List<TurnResults> results){
		for(TurnResults result: results){
			logResults(result);

			if (result.getDamageTaken() > 0){
				FloatingText text = new FloatingText();
				BattleScene scene = Util.getScene(Scenes.BATTLE, BattleScene.class);
				text.setLocation(scene.getSpatialByUnit(result.getUnit()).getNode().getLocalTranslation().add(0, 8f, 0))
					.setText("-" + String.valueOf(result.getDamageTaken()))
					.setVelocity(new Vector3f(0, 2.0f, 0))
					.setColor(ColorRGBA.Red)
					.setSize(1.3f)
					.setFadeTime(3f);

				scene.addFloatingText(text);
			}

			if (result.getHealDone() > 0){
				FloatingText text = new FloatingText();
				BattleScene scene = Util.getScene(Scenes.BATTLE, BattleScene.class);
				text.setLocation(scene.getSpatialByUnit(result.getUnit()).getNode().getLocalTranslation().add(0, 6f, 0))
						.setText("+" + String.valueOf(result.getHealDone()))
						.setVelocity(new Vector3f(0, 2.0f, 0))
						.setColor(ColorRGBA.Green)
						.setSize(1.3f)
						.setFadeTime(3f);

				scene.addFloatingText(text);
			}
		}

		program.getMainFrame().enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				Util.getUI(UI.STATE_BATTLE, BattleUIState.class).updateFromBoard();
				updateUnitUI(state);
				return null;
			}
		});
	}

	public void initBattle(final int boardId, final BoardSetup setup, final CardMaster... cardMasters){
		Runnable runnable = new Runnable() {
			public void run() {
				Board board = new Board(setup.getWidth(), setup.getHeight());
				board.setId(boardId);
				board.setPlacementArea(setup.getPlacementAreas());
				program.getMainFrame().setScene(Scenes.BATTLE);

				for(CardMaster cm: cardMasters){
					board.addCardMaster(cm);
					cm.setState(CardMaster.STATE_IN_BATTLE);
					cm.setCurrentBoard(board);
				}

				int index = 0;
				for (Integer[] alliance: setup.getAlliances()){
					Alliance toAdd = new Alliance();
					toAdd.setId(index++);
					for (int id: alliance)
						toAdd.addCardMaster(board.getCardMasters().get(id));
					board.addAlliance(toAdd);
				}

				board.setTurnTime(setup.getTurnTime());

				board.selectTurningPlayer();

				program.getMainFrame().setUIState(UI.STATE_BATTLE);
				program.getMainFrame().addUIState(UI.STATE_BATTLE_PICK_INTERFACE);

				program.setClientState(Program.STATE_BATTLE);

				BattleState state = new BattleState();
				state.setBoard(board);

				Util.getScene(Scenes.BATTLE, BattleScene.class).setBattleState(state);
				Util.getUI(UI.STATE_BATTLE, BattleUIState.class).setBattleState(state);
				Util.getUI(UI.STATE_BATTLE_PICK_INTERFACE, BattlePickUIState.class).setBattleState(state);

				battleStates.put(boardId, state);
			}
		};

		DataStore.getInstance().awaitAndExecute(runnable, new ActualCondition(DataKey.INVENTORY), new ExistenceCondition(DataKey.MAIN_PLAYER));

		program.updateInventory();
	}

	public void endBattle(BattleState battleState){
		Board board = battleState.getBoard();

		Util.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().clear();
		program.setClientState(Program.STATE_GLOBAL_MAP);
		program.getMainFrame().setScene(Scenes.MAIN_MAP);

		for(CardMaster master: board.getCardMasters()){
			master.setState(CardMaster.STATE_IN_GLOBAL_MAP);
			master.setCurrentBoard(null);
		}

		program.getMainFrame().setUIState(UI.STATE_MAP_MAIN);

		battleState.setSelectedUnit(null);
	}

	public void battleSkipTurn(){
		try {
			Packet packet = new Packet(Program.HEADER_BATTLE_SKIP_TURN);
			program.getLocalClient().send(packet);
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}

	public void battlePlacementFinishedLocal(BattleState state) {
		try {
			state.getBoard().finishPlacementLocal();
			Packet packet = new Packet(Program.HEADER_PLACEMENT_FINISHED);
			program.getLocalClient().send(packet);
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}

	public void battlePlacementFinished(BattleState state){
		state.getBoard().finishPlacementPhase();
		program.getMainFrame().removeUIState(UI.STATE_BATTLE_PLACEMENT_INTERFACE);
	}

	public void battleMoveUnit(Board board, Unit unit, Cell position){
		Cell selectedCell = unit.getPosition();

		byte data[] = new byte[]{
			(byte) selectedCell.getX(),
			(byte) selectedCell.getY(),
			(byte) position.getX(),
			(byte) position.getY()
		};

		try {
			if (board.getState() == Board.STATE_WAIT_FOR_PLACEMENT)
				program.getLocalClient().send(new Packet(Program.HEADER_BATTLE_PLACE_ORDER, data));
			else
				program.getLocalClient().send(new Packet(Program.HEADER_BATTLE_MOVE_ORDER, data));
		} catch (IncorrectHeaderException e1) {
			e1.printStackTrace();
		}
	}

	public void battleAttackUnit(Board board, Unit unit, Unit target){
		battleMoveUnit(board, unit, target.getPosition()); // TODO lol every time you see this
	}

	public void battleCast(Unit caster, Cell target, int spell){
		Cell selectedCell = caster.getPosition();

		byte data[] = new byte[]{
			(byte) spell,
			(byte) selectedCell.getX(),
			(byte) selectedCell.getY(),
			(byte) target.getX(),
			(byte) target.getY()
		};

		try {
			program.getLocalClient().send(new Packet(Program.HEADER_SPELL_CAST_ORDER, data));
		} catch (IncorrectHeaderException e1) {
			e1.printStackTrace();
		}
	}

	public void battleCastCard(SpellCardItem card){
		try {
			program.getLocalClient().send(new Packet(Program.HEADER_CARD_CAST_ORDER, DataUtil.intToByte(card.getId())));
		} catch (IncorrectHeaderException e1) {
			e1.printStackTrace();
		}
	}

	public void abilityButtonClicked(BattleState state, int number){
		MainFrame mainFrame = program.getMainFrame();
		updateUnitUI(state);

		Unit selectedUnit = state.getSelectedUnit();
		Spell spell = selectedUnit.getSpells().get(number);

		if (spell.getSpellData().onlyAllowed(SpellTarget.SELF)){
			battleCast(selectedUnit, selectedUnit.getPosition(), number);
		} else {
			state.setIsCastMode(true);
			state.setSpellToCastNumber(number);
			state.setSpellToCast(spell);
			mainFrame.setCursor(UI.CURSOR_CAST);
		}
		updateUnitUI(state);
	}

	public void updateUnitUI(BattleState state) {
		Util.getUI(UI.STATE_BATTLE, BattleUIState.class).updateUnitUI(state.getSelectedUnit());
	}

	public void endCast(BattleState state){
		state.setIsCastMode(false);
		program.getMainFrame().setCursor(UI.CURSOR_DEFAULT);
		updateUnitUI(state);
	}
}
