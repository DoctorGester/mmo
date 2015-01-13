package core.board.events;

import core.board.interfaces.Cell;
import core.board.interfaces.Spell;
import core.board.interfaces.Unit;

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
