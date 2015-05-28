import shared.board.Board
import shared.board.Cell
import shared.board.Ability
import shared.other.DataUtil

def onCheck(Ability spell, Board board, Cell target){
	DataUtil.distance(spell.caster.position, target) <= 5
}

def onCast(Ability spell, Board board, Cell target){
	def units = board.units.findAll { it != spell.caster && DataUtil.distance(it.position, target) <= 3 }

	board.addBuff("SilenceBuff", 1, 6, units)
	board.nextTurn()
	spell.putOnCoolDown()
}