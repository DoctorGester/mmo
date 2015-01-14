import shared.board.Board
import shared.board.Cell
import shared.board.Spell
import shared.board.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Spell spell, Board board, Cell target){
	target.getUnit().doHeal(6)
	board.nextTurn()
	spell.putOnCoolDown()
}