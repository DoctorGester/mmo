package core.board;

/**
 * @author doc
 */
public class TurnResults {
	private Unit unit;
	private int healDone, damageTaken;

	public TurnResults(Unit unit, int damageTaken, int healDone) {
		this.unit = unit;
		this.healDone = healDone;
		this.damageTaken = damageTaken;
	}

	public Unit getUnit() {
		return unit;
	}

	public int getHealDone() {
		return healDone;
	}

	public int getDamageTaken() {
		return damageTaken;
	}
}
