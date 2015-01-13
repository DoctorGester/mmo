package core.board.turns;

import core.board.Board;
import core.board.Cell;
import core.board.Unit;
import core.ui.BattleState;
import program.main.Program;

/**
 * @author doc
 */
public class TurnFinishPlacement implements Turn {
	private BattleState battleState;
	private byte[] data;

	public TurnFinishPlacement(BattleState battleState, byte data[]){
		this.battleState = battleState;
		this.data = data;
	}

	public void execute(int mode) {
		if (mode != MODE_FIRST_STEP)
			return;

		Program program = Program.getInstance();

		Board board = battleState.getBoard();
		for (int i = 0; i < data.length; i += 2){
			int x = data[i],
				y = data[i + 1],
				number = i / 2;

			Unit unit = board.getUnits().get(number);

			Cell position = board.getCell(x, y);

			if (position.getContentsType() == Cell.CONTENTS_UNIT){
				// If cell is occupied, swap places
				unit.swapPositionWith(position.getUnit());
			} else {
				unit.setPosition(position);
			}
			unit.resetWavePath();
		}

		program.getBattleController().battlePlacementFinished(battleState);
	}

	public void update(float tpf) {
	}

	public boolean hasLastStep() {
		return false;
	}

	public boolean firstStepFinished(){
		return true;
	}

	@Override
	public String toStringRepresentation() {
		return "Placement is finished.";
	}
}