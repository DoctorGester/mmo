package shared.board.events;

import shared.board.Cell;
import shared.board.Ability;
import shared.board.Unit;

public class CastEventContext {
	private Cell target;
	private Unit source;
	private Ability ability;

	public CastEventContext(Unit source, Cell target, Ability ability) {
		this.target = target;
		this.source = source;
		this.ability = ability;
	}

	public Cell getTarget() {
		return target;
	}

	public Ability getAbility() {
		return ability;
	}

	public Unit getSource() {
		return source;
	}
}
