package core.board.turns;

import core.board.ClientBoard;
import core.board.ClientUnit;
import core.ui.UI;
import core.ui.battle.BattlePickUIState;
import program.main.Program;
import program.main.SceneUtil;
import shared.board.Board;
import shared.board.Cell;
import shared.board.data.UnitData;
import shared.items.types.UnitCardItem;
import shared.map.CardMaster;

import java.awt.*;

/**
 * @author doc
 */
public class TurnPick implements Turn {
	private static final float WAIT_TIME = 5f;

	private ClientBoard board;
	private CardMaster cardMaster;
	private UnitCardItem card;

	private float timePassed = 0f;

	public TurnPick(ClientBoard board, CardMaster cardMaster, UnitCardItem card) {
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

					ClientUnit unit = new ClientUnit(cardMaster, unitData, board.getCell(x, y));
					unit.setFacingInstantly(ClientBoard.DEFAULT_FACING[cardMaster.getBattleId()]);

					// Update UI
					BattlePickUIState pick = SceneUtil.getUI(UI.STATE_BATTLE_PICK_INTERFACE, BattlePickUIState.class);

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
		boolean interfaceUpdated = SceneUtil.getUI(UI.STATE_BATTLE_PICK_INTERFACE, BattlePickUIState.class).isCardPlacementFinished();

		return !pickFinished || (interfaceUpdated && enoughTimePassed);
	}

	@Override
	public String toStringRepresentation() {
		UnitData unitData = Program.getInstance().getUnitDataById(card.getUnitId());

		return cardMaster.getName() + " picks " + unitData.getName();
	}
}
