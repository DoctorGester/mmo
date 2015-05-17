import shared.board.Board
import shared.board.Spell
import shared.board.Unit
import shared.map.CardMaster

def onCast(Spell spell, Board board, CardMaster caster){
    for (Unit unit: board.units)
        if (unit.state != Unit.STATE_DEAD)
            unit.purgeBuffs()

    board.nextTurn()
}
