import shared.board.Board
import shared.board.CardSpell
import shared.board.Unit
import shared.map.CardMaster

def onCast(CardSpell spell, Board board, CardMaster caster){
	for (Unit unit: board.units)
        if (board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD)
            board.addBuff("bonusDamageBuff", 0, Integer.MAX_VALUE, [
                "unit": unit,
                "value": 2
            ])

    board.nextTurn()
}