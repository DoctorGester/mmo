package core.ui;

import core.board.ClientBoard;
import core.board.ClientSpell;
import core.board.ClientUnit;

public class BattleState {
	private ClientBoard board;
	private ClientUnit selectedUnit;
	private boolean isCastMode;
	private int spellToCastNumber = -1;
	private ClientSpell spellToCast;

	public ClientBoard getBoard() {
		return board;
	}

	public void setBoard(ClientBoard board) {
		this.board = board;
	}

	public ClientUnit getSelectedUnit() {
		return selectedUnit;
	}

	public void setSelectedUnit(ClientUnit selectedUnit) {
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

	public ClientSpell getSpellToCast() {
		if (!isCastMode)
			return null;
		return spellToCast;
	}

	public void setSpellToCast(ClientSpell spellToCast) {
		this.spellToCast = spellToCast;
	}
}
