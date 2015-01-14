import shared.board.Board
import shared.board.Cell
import shared.board.Spell
import shared.other.DataUtil

def onCheck(Spell spell, Board board, Cell target){
    DataUtil.distance(spell.caster.position, target) <= 5
}

def onCast(Spell spell, Board board, Cell target){
    def units = board.units.findAll { it != spell.caster && DataUtil.distance(it.position, target) <= 3 }

    board.addBuff("silenceBuff", 1, 6, units)
	board.nextTurn()
	spell.putOnCoolDown()
}