package core.board.events;

import core.board.DamageType;
import core.board.Unit;

public class DamageEventContext {
	private Unit target;
	private int damage;
	private DamageType type;

	private int reduction, amplification;

	public DamageEventContext(Unit target, int damage, DamageType type) {
		this.target = target;
		this.damage = damage;
		this.type = type;
	}

	public Unit getTarget() {
		return target;
	}

	public int getDamage() {
		return damage;
	}

	public DamageType getType() {
		return type;
	}

	public void applyDamageReduction(int reduction){
		this.reduction += reduction;
	}

	public void applyDamageAmplification(int amplification){
		this.amplification += amplification;
	}

	public int getResultDamage(){
		return Math.max(0, damage + amplification - reduction);
	}
}
