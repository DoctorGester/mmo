import core.board.ClientBoard
import shared.board.CardSpell
import shared.board.Board
import shared.board.Cell
import shared.board.Unit
import shared.map.CardMaster

def onCheck(CardSpell spell, Board board, CardMaster caster, Cell cell){
    cell.contentsType == Cell.CONTENTS_UNIT && cell.unit.state != Unit.STATE_DEAD && cell.unit.buffs.size() > 0
}

def onCastBegin(CardSpell spell, ClientBoard board, CardMaster caster){
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

