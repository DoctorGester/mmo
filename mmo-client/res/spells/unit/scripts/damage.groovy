import core.board.Board
import core.board.Cell
import core.board.DamageType
import core.board.Spell
import core.board.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(Spell spell, Board board, Cell target){
	spell.caster.setFacing(target.unit);
	0.5F
}

def onCastEnd(Spell spell, Board board, Cell target){
	target.unit.doDamage(2, DamageType.SPELL)
	board.nextTurn()
	spell.putOnCoolDown()
}