import core.board.Board
import core.board.CardSpell
import core.board.Cell
import core.board.Unit
import core.main.CardMaster

def onCheck(CardSpell spell, Board board, CardMaster caster, Cell cell){
    cell.contentsType == Cell.CONTENTS_UNIT && cell.unit.state != Unit.STATE_DEAD && cell.unit.buffs.size() > 0
}

def onCastBegin(CardSpell spell, Board board, CardMaster caster){
    for (Unit unit: board.units)
        if (unit.state != Unit.STATE_DEAD && unit.buffs.size() > 0){
            board.addEffect("purge-effect", unit)
            unit.purgeBuffs()
        }

    1.0f
}

def onCastEnd(CardSpell spell, Board board, CardMaster caster){
    board.nextTurn()
}

