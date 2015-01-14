package shared.board.events;

import shared.board.Cell;
import shared.board.Spell;
import shared.board.Unit;

public class CastEventContext {
	private Cell target;
	private Unit source;
	private Spell spell;

	public CastEventContext(Unit source, Cell target, Spell spell) {
		this.target = target;
		this.source = source;
		this.spell = spell;
	}

	public Cell getTarget() {
		return target;
	}

	public Spell getSpell() {
		return spell;
	}

	public Unit getSource() {
		return source;
	}
}
