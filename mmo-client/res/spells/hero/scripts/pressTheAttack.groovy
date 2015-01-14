import core.board.CardSpell
import shared.board.Board
import shared.board.Cell
import shared.board.Unit
import shared.map.CardMaster

def onCheck(CardSpell spell, Board board, CardMaster caster, Cell cell){
    cell.contentsType == Cell.CONTENTS_UNIT && board.areAllies(cell.unit.owner, caster) && cell.unit.state != Unit.STATE_DEAD
}

def onCastBegin(CardSpell spell, Board board, CardMaster caster){
    for (Unit unit: board.units)
        if (board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD){
            board.addEffect("heal-effect", unit)
            board.addBuff("bonusDamageBuff", 0, Integer.MAX_VALUE, [
                    "unit": unit,
                    "value": 2
            ])
        }
    1.0f
}

def onCastEnd(CardSpell spell, Board board, CardMaster caster){
    board.nextTurn()
}