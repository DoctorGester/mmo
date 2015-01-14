import core.board.ClientCell
import core.board.ClientSpell
import shared.board.Board
import shared.board.Cell
import shared.board.DamageType
import shared.board.Spell
import shared.board.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(ClientSpell spell, Board board, ClientCell target){
	spell.caster.setFacing(target.unit);
	0.5F
}

def onCastEnd(Spell spell, Board board, Cell target){
	target.unit.doDamage(2, DamageType.SPELL)
	board.nextTurn()
	spell.putOnCoolDown()
}