import shared.board.Board
import shared.board.Spell
import shared.board.Unit
import shared.map.CardMaster

def onCast(Spell spell, Board board, CardMaster caster){
	for (Unit unit: board.units)
		if (!board.areAllies(unit.owner, caster) && unit.state != Unit.STATE_DEAD)
			unit.maxActionPoints -= 1;

	board.nextTurn()
}
