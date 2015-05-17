import shared.board.Board
import shared.board.Cell
import shared.board.DamageType
import shared.board.Ability
import shared.board.Unit

def onCheck(Ability spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Ability spell, Board board, Cell target){
    target.getUnit().doDamage(2, DamageType.MAGIC)
	board.nextTurn()
	spell.putOnCoolDown()
}