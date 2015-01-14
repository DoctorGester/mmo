package core.board.turns;

import shared.board.Cell;

/**
 * @author doc
 */
public class TurnPlace implements Turn {
	private Cell from;
	private Cell to;

	public TurnPlace(){}

	public TurnPlace(Cell from, Cell to){
		this.from = from;
		this.to = to;
	}

	public void execute(int mode) {
		if (mode == MODE_FIRST_STEP && from != null)
			from.getUnit().setPosition(to);
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
		return "Unit moved";
	}
}
