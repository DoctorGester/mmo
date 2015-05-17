package core.board.turns;

import core.board.ClientSpell;
import shared.board.Spell;
import program.main.Program;
import shared.board.Board;
import shared.board.data.SpellData;
import shared.items.types.SpellCardItem;
import shared.map.CardMaster;

/**
 * @author doc
 */
public class TurnCastSpell implements Turn {

	private final Board board;
	private CardMaster caster;
	private SpellCardItem item;

	private boolean finished = false;
	private float waitForCastTime;

	public TurnCastSpell(Board board, CardMaster caster, SpellCardItem item){
		this.board = board;
		this.caster = caster;
		this.item = item;
	}

	public void execute(int mode) {
		SpellData spellData = Program.getInstance().getCardSpellDataById(item.getSpellId());
		Spell spell = new ClientSpell(spellData, caster, board);
		switch (mode){
			case MODE_FIRST_STEP:{
				waitForCastTime = ((Number) spell.callEvent(Spell.SCRIPT_EVENT_CAST_BEGIN)).floatValue();
				break;
			}
			case MODE_LAST_STEP:{
				spell.callEvent(Spell.SCRIPT_EVENT_CAST_END);
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
