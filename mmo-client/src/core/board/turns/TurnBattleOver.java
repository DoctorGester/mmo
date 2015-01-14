package core.board.turns;

import core.ui.BattleState;
import core.ui.UI;
import core.ui.battle.BattleOverUIState;
import program.main.Program;
import program.main.SceneUtil;
import shared.map.CardMaster;

import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class TurnBattleOver implements Turn {
	private static final float WAIT_TIME = 3f;

	private BattleState battleState;
	private CardMaster[] winners;
	private int status;
	private float timePassed;

	private boolean windowVisible = false;

	public TurnBattleOver(BattleState battleState, int status, CardMaster... winners){
		this.battleState = battleState;
		this.status = status;
		this.winners = winners;
	}

	public void execute(int mode) {
		if (mode == MODE_LAST_STEP)
			Program.getInstance().enqueue(new Callable() {
				public Object call() throws Exception {
					Program.getInstance().getBattleController().endBattle(battleState);
					return null;
				}
			});
	}

	public void update(float tpf) {
		timePassed += tpf;
		if (!windowVisible && timePassed >= WAIT_TIME){
			windowVisible = true;
			Program.getInstance().getMainFrame().setUIState(UI.STATE_BATTLE_OVER);
			SceneUtil.getUI(UI.STATE_BATTLE_OVER, BattleOverUIState.class).setStatus(status, winners);
		}
	}

	public boolean hasLastStep() {
		return true;
	}

	public boolean firstStepFinished(){
		return windowVisible && SceneUtil.getUI(UI.STATE_BATTLE_OVER, BattleOverUIState.class).exitWasClicked();
	}

	@Override
	public String toStringRepresentation() {
		return "Battle is over!";
	}
}
