import core.board.Board
import core.board.CardSpell
import core.board.Cell
import core.board.Unit
import core.main.CardMaster

def onCheck(CardSpell spell, Board board, CardMaster caster, Cell cell){
    cell.contentsType == Cell.CONTENTS_UNIT && !board.areAllies(cell.unit.owner, caster) && cell.unit.state != Unit.STATE_DEAD
}

def onCastBegin(CardSpell spell, Board board, CardMaster caster){
    0.3f
}

def onCastEnd(CardSpell spell, Board board, CardMaster caster){
    for (Unit unit: board.units)
        if (!board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD)
            unit.maxActionPoints -= 1;

    board.nextTurn()
}