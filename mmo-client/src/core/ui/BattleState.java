package core.ui;

import core.board.Board;
import core.board.Spell;
import core.board.Unit;

public class BattleState {
	private Board board;
	private Unit selectedUnit;
	private boolean isCastMode;
	private int spellToCastNumber = -1;
	private Spell spellToCast;

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Unit getSelectedUnit() {
		return selectedUnit;
	}

	public void setSelectedUnit(Unit selectedUnit) {
		this.selectedUnit = selectedUnit;
	}

	public boolean isCastMode() {
		return isCastMode;
	}

	public void setIsCastMode(boolean castMode) {
		this.isCastMode = castMode;
	}

	public int getSpellToCastNumber() {
		return spellToCastNumber;
	}

	public void setSpellToCastNumber(int spellToCastNumber) {
		this.spellToCastNumber = spellToCastNumber;
	}

	public Spell getSpellToCast() {
		if (!isCastMode)
			return null;
		return spellToCast;
	}

	public void setSpellToCast(Spell spellToCast) {
		this.spellToCast = spellToCast;
	}
}
