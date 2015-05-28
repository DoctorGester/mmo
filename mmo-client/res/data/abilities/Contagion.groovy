import core.board.ClientCell
import core.board.ClientAbility
import shared.board.Board
import shared.board.Buff
import shared.board.Cell
import shared.board.Ability
import shared.board.Unit
import shared.other.DataUtil

def onCheck(Ability spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD && DataUtil.distance(spell.caster, target.unit) <= 2
}

def onCheckAOE(Ability spell, Board board, Cell from, Cell to){
	def x = Math.abs(from.x - to.x)
	def y = Math.abs(from.y - to.y)

	(x <= 1 && y <= 1)
}

def onCastBegin(ClientAbility spell, Board board, ClientCell target){
	spell.caster.setFacing(target.unit);
	0.5F
}

def onCastEnd(Ability spell, Board board, Cell target){
	Buff buff = board.addBuff("ContagionBuff", 3, 2, target.unit)
	target.unit.addBuff(buff)
	board.nextTurn()
	spell.putOnCoolDown()
}