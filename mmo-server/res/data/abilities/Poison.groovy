import shared.board.Board
import shared.board.Cell
import shared.board.Ability
import shared.board.Unit

def onCheck(Ability spell, Board board, Cell target){
	target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Ability spell, Board board, Cell target){
	board.addBuff("PoisonBuff", 4, 1, target.getUnit())
	board.nextTurn()
	spell.putOnCoolDown()
}