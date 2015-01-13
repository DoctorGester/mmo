import core.board.interfaces.Board
import core.board.interfaces.Cell
import core.board.interfaces.Spell
import program.main.Util

def onCheck(Spell spell, Board board, Cell target){
    Util.distance(spell.caster.position, target) <= 5
}

def onCast(Spell spell, Board board, Cell target){
    def units = board.units.findAll { it != spell.caster && Util.distance(it.position, target) <= 3 }

    board.addBuff("silenceBuff", 1, 6, units)
	board.nextTurn()
	spell.putOnCoolDown()
}