import shared.board.Board
import shared.board.Buff
import shared.board.Cell
import shared.board.Spell
import shared.board.Unit
import shared.other.DataUtil

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD && DataUtil.distance(spell.caster, target.unit) <= 2
}

def onCast(Spell spell, Board board, Cell target){
	Buff buff = board.addBuff("contagionBuff", 3, 2, target.unit)
	target.unit.addBuff(buff)
	board.nextTurn()
	spell.putOnCoolDown()
}