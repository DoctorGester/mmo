package core.board.turns;

import core.board.ClientCell;
import core.board.ClientUnit;
import shared.board.Board;
import shared.board.Cell;
import shared.board.Spell;
import shared.board.Unit;

/**
 * @author doc
 */
public class TurnCast implements Turn {

	private final Board board;
	private final ClientCell from;
	private final Cell to;
	private final int spell;
	private ClientUnit ordered;

	private boolean finished = false;
	private float waitForCastTime;

	public TurnCast(Board board, ClientCell from, Cell to, int spell){
		this.board = board;
		this.from = from;
		this.to = to;
		this.spell = spell;
	}

	public void execute(int mode) {
		ordered = from.getUnit();
		Spell toCast = ordered.getSpells().get(spell);
		switch (mode){
			case MODE_FIRST_STEP:{
				waitForCastTime = ((Number) toCast.callEvent(Spell.SCRIPT_EVENT_CAST_BEGIN, to)).floatValue();
				board.getCurrentTurningPlayer().setUsedUnit(ordered);
				break;
			}
			case MODE_LAST_STEP:{
				ordered.fireCastEvent(to, toCast);
				toCast.callEvent(Spell.SCRIPT_EVENT_CAST_END, to);
				break;
			}
		}
	}

	public void update(float tpf) {
		ordered.updateFacing();
		waitForCastTime -= tpf;
		if (waitForCastTime <= 0)
			finished = true;
	}

	public boolean hasLastStep() {
		return true;
	}

	public boolean firstStepFinished(){
		return finished;
	}

	@Override
	public String toStringRepresentation() {
		String to;

		if (this.to.getContentsType() == Cell.CONTENTS_EMPTY)
			to = this.to.toString();
		else
			to = this.to.getUnit().getUnitData().getName();

		return ordered.getOwner().getName() +
				" uses " +
				ordered.getUnitData().getName() +
				" to cast " +
				ordered.getSpells().get(spell).getSpellData().getId() +
				" on " +
				to;
	}
}
