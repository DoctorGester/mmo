package core.board.turns;

import core.board.Board;
import core.board.CardSpell;
import core.board.CardSpellData;
import core.main.CardMaster;
import core.main.inventory.items.SpellCardItem;
import program.main.Program;

/**
 * @author doc
 */
public class TurnCastCardSpell implements Turn {

	private final Board board;
	private CardMaster caster;
	private SpellCardItem item;

	private boolean finished = false;
	private float waitForCastTime;

	public TurnCastCardSpell(Board board, CardMaster caster, SpellCardItem item){
		this.board = board;
		this.caster = caster;
		this.item = item;
	}

	public void execute(int mode) {
		CardSpellData spellData = Program.getInstance().getCardSpellDataById(item.getSpellId());
		CardSpell cardSpell = new CardSpell(spellData, caster, board);
		switch (mode){
			case MODE_FIRST_STEP:{
				waitForCastTime = ((Number) cardSpell.callEvent(CardSpell.SCRIPT_EVENT_CAST_BEGIN)).floatValue();
				break;
			}
			case MODE_LAST_STEP:{
				cardSpell.callEvent(CardSpell.SCRIPT_EVENT_CAST_END);
				break;
			}
		}
	}

	public void update(float tpf) {
		waitForCastTime -= tpf;
		if (waitForCastTime <= 0)
			finished = true;
	}

	public boolean hasLastStep() {
		return true;
	}

	public boolean firstStepFinished(){
		return finished;
	}

	@Override
	public String toStringRepresentation() {
		return caster.getName() + " uses " + Program.getInstance().getCardSpellDataById(item.getSpellId()).getName() + " card";
	}
}
