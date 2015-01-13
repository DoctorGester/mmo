package core.board.turns;

import core.ui.UI;
import core.ui.battle.BattleLogUIState;
import program.main.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class TurnQueue {
	private final Map<Short, Turn> turns;
	private Turn currentTurn;

	private short turnNumber;

	private boolean firstStepExecuted;

	public TurnQueue(){
		turns = new HashMap<Short, Turn>();
	}

	private void next(){
		Util.getUI(UI.STATE_BATTLE_LOG, BattleLogUIState.class).log(currentTurn.toStringRepresentation());
		turnNumber++;
		currentTurn = turns.get(turnNumber);
	}

	public void clear(){
		synchronized (turns){
			turns.clear();
			currentTurn = null;
			turnNumber = 0;
			firstStepExecuted = false;
		}
	}

	public void add(short number, Turn turn){
		turns.put(number, turn);
	}

	public void update(float tpf){
		synchronized (turns){
			if (currentTurn == null){
				currentTurn = turns.get(turnNumber);
				if (currentTurn == null)
					return;
			}

			if (currentTurn.hasLastStep()){
				if (!firstStepExecuted){
					currentTurn.execute(Turn.MODE_FIRST_STEP);
					firstStepExecuted = true;
				}
				currentTurn.update(tpf);
				if (currentTurn.firstStepFinished()){
					currentTurn.execute(Turn.MODE_LAST_STEP);
					firstStepExecuted = false;
					next();
				}
			} else {
				currentTurn.execute(Turn.MODE_FIRST_STEP);
				currentTurn.execute(Turn.MODE_LAST_STEP);
				next();
			}
		}
	}
}
