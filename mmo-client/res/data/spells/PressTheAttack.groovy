import core.board.ClientBoard
import shared.board.Spell
import shared.board.Board
import shared.board.Cell
import shared.board.Unit
import shared.map.CardMaster

def onCheck(Spell spell, Board board, CardMaster caster, Cell cell){
    cell.contentsType == Cell.CONTENTS_UNIT && board.areAllies(cell.unit.owner, caster) && cell.unit.state != Unit.STATE_DEAD
}

def onCastBegin(Spell spell, ClientBoard board, CardMaster caster){
    for (Unit unit: board.units)
        if (board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD){
            board.addEffect("heal-effect", unit)
            board.addBuff("BonusDamageBuff", 0, Integer.MAX_VALUE, [
                    "unit": unit,
                    "value": 2
            ])
        }
    1.0f
}

def onCastEnd(Spell spell, Board board, CardMaster caster){
    board.nextTurn()
}