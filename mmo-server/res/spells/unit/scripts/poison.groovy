import shared.board.Board
import shared.board.Cell
import shared.board.Spell
import shared.board.Unit

def onCheck(Spell spell, Board board, Cell target){
    target.getUnit().getState() != Unit.STATE_DEAD
}

def onCast(Spell spell, Board board, Cell target){
    board.addBuff("poisonBuff", 4, 1, target.getUnit())
    board.nextTurn()
    spell.putOnCoolDown()
}