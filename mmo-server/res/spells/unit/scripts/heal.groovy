import core.board.interfaces.Board
import core.board.interfaces.Cell
import core.board.interfaces.Spell
import core.board.interfaces.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Spell spell, Board board, Cell target){
	target.getUnit().doHeal(6)
	board.nextTurn()
	spell.putOnCoolDown()
}