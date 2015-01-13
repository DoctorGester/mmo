package core.board.events;

import core.board.interfaces.Unit;

public class HealEventContext {
	private Unit target;
	private int healing;

	private int reduction, amplification;

	public HealEventContext(Unit target, int healing) {
		this.target = target;
		this.healing = healing;
	}

	public Unit getTarget() {
		return target;
	}

	public int getHealing() {
		return healing;
	}

	public void applyHealingReduction(int reduction){
		this.reduction += reduction;
	}

	public void applyHealingAmplification(int amplification){
		this.amplification += amplification;
	}

	public int getResultHealing(){
		return Math.max(0, healing + amplification - reduction);
	}
}
