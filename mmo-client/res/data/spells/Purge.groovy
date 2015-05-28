import core.board.ClientBoard
import shared.board.Spell
import shared.board.Board
import shared.board.Cell
import shared.board.Unit
import shared.map.CardMaster

def onCheck(Spell spell, Board board, CardMaster caster, Cell cell){
    cell.contentsType == Cell.CONTENTS_UNIT && cell.unit.state != Unit.STATE_DEAD && cell.unit.buffs.size() > 0
}

def onCastBegin(Spell spell, ClientBoard board, CardMaster caster){
    for (Unit unit: board.units)
        if (unit.state != Unit.STATE_DEAD && unit.buffs.size() > 0){
            board.addEffect("purge-effect", unit)
            unit.purgeBuffs()
        }

    1.0f
}

def onCastEnd(Spell spell, Board board, CardMaster caster){
    board.nextTurn()
}

