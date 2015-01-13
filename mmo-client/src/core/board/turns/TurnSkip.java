package core.board.turns;

import core.board.Board;

/**
 * @author doc
 */
public class TurnSkip implements Turn {
	private Board board;

	public TurnSkip(Board board){
		this.board = board;
	}

	public void execute(int mode) {
		if (mode == MODE_FIRST_STEP)
			board.nextTurn();
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
		return "Turn is skipped";
	}
}
