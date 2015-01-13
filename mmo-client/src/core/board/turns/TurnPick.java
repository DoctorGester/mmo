package core.board.turns;

import core.board.Board;
import core.board.Cell;
import core.board.Unit;
import core.board.UnitData;
import core.main.CardMaster;
import core.main.inventory.items.CardItem;
import core.ui.UI;
import core.ui.battle.BattlePickUIState;
import program.main.Program;
import program.main.Util;

import java.awt.*;

/**
 * @author doc
 */
public class TurnPick implements Turn {
	private static final float WAIT_TIME = 5f;

	private Board board;
	private CardMaster cardMaster;
	private CardItem card;

	private float timePassed = 0f;

	public TurnPick(Board board, CardMaster cardMaster, CardItem card) {
		this.board = board;
		this.cardMaster = cardMaster;
		this.card = card;
	}

	public void execute(int mode) {
		if (mode != Turn.MODE_FIRST_STEP){
			// Switch board state if pick stage has ended
			if (board.getUnits().size() == board.getCardMasters().size() * Board.OWNED_UNITS){
				board.setState(Board.STATE_WAIT_FOR_PLACEMENT);
				Program.getInstance().getBattleController().onPickStageEnd(board);
			}

			board.nextTurn();
			return;
		}

		Rectangle area = board.getPlacementArea()[cardMaster.getBattleId()];

		for(int y = area.y; y < area.y + area.height; y++)
			for(int x = area.x; x < area.x + area.width; x++){
				if (board.getCell(x, y).getContentsType() == Cell.CONTENTS_EMPTY){
					UnitData unitData = Program.getInstance().getUnitDataById(card.getUnitId());

					Unit unit = new Unit(cardMaster, unitData, board.getCell(x, y));
					unit.setFacingInstantly(Board.DEFAULT_FACING[cardMaster.getBattleId()]);

					// Update UI
					BattlePickUIState pick = Util.getUI(UI.STATE_BATTLE_PICK_INTERFACE, BattlePickUIState.class);

					pick.addPickedCard(card);

					return;
				}
			}
	}

	public void update(float tpf) {
		timePassed += tpf;
	}

	public boolean hasLastStep() {
		return true;
	}

	public boolean firstStepFinished(){
		boolean enoughTimePassed = timePassed >= WAIT_TIME;
		boolean pickFinished = board.getUnits().size() == board.getCardMasters().size() * Board.OWNED_UNITS;
		boolean interfaceUpdated = Util.getUI(UI.STATE_BATTLE_PICK_INTERFACE, BattlePickUIState.class).isCardPlacementFinished();

		return !pickFinished || (interfaceUpdated && enoughTimePassed);
	}

	@Override
	public String toStringRepresentation() {
		UnitData unitData = Program.getInstance().getUnitDataById(card.getUnitId());

		return cardMaster.getName() + " picks " + unitData.getName();
	}
}
