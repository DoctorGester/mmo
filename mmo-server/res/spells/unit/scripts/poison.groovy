import core.board.interfaces.Board
import core.board.interfaces.Cell
import core.board.interfaces.Spell
import core.board.interfaces.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Spell spell, Board board, Cell target){
    board.addBuff("poisonBuff", 4, 1, target.getUnit())
    board.nextTurn()
    spell.putOnCoolDown()
}