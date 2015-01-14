import shared.board.Board
import shared.board.Cell
import shared.board.DamageType
import shared.board.Spell
import shared.board.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Spell spell, Board board, Cell target){
    target.getUnit().doDamage(2, DamageType.SPELL)
	board.nextTurn()
	spell.putOnCoolDown()
}