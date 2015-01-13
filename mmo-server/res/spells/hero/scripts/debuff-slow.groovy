import core.board.interfaces.Board
import core.board.CardSpell
import core.board.interfaces.Cell
import core.board.interfaces.Unit
import core.main.CardMaster

def onCast(CardSpell spell, Board board, CardMaster caster){
    for (Unit unit: board.units)
        if (!board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD)
            unit.maxActionPoints -= 1;

    board.nextTurn()
}
