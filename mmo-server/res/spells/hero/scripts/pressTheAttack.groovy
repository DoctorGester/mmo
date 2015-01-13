import core.board.interfaces.Board
import core.board.CardSpell
import core.board.interfaces.Unit
import core.main.CardMaster

def onCast(CardSpell spell, Board board, CardMaster caster){
	for (Unit unit: board.units)
        if (board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD)
            board.addBuff("bonusDamageBuff", 0, Integer.MAX_VALUE, [
                "unit": unit,
                "value": 2
            ])

    board.nextTurn()
}