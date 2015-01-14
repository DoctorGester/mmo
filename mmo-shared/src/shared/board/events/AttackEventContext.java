package shared.board.events;

import shared.board.Cell;
import shared.board.Unit;

public class AttackEventContext {
	private Unit source;
	private Cell target;

	private int reduction, amplification;

	public AttackEventContext(Unit source, Cell target) {
		this.source = source;
		this.target = target;
	}

	public Unit getSource() {
		return source;
	}

	public Cell getTarget() {
		return target;
	}

	public void applyDamageReduction(int reduction){
		this.reduction += reduction;
	}

	public void applyDamageAmplification(int amplification){
		this.amplification += amplification;
	}

	public int getResultDamage(){
		return Math.max(0, source.getTotalAttackDamage() + amplification - reduction);
	}
}
