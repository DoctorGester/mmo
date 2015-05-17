import core.board.ClientCell
import core.board.ClientAbility
import shared.board.Board
import shared.board.Cell
import shared.board.DamageType
import shared.board.Ability
import shared.board.Unit

def onCheck(Ability spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCheckAOE(Ability spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(ClientAbility spell, Board board, ClientCell target){
	spell.caster.setFacing(target.unit);
	0.5F
}

def onCastEnd(Ability spell, Board board, Cell target){
	target.unit.doDamage(2, DamageType.MAGIC)
	board.nextTurn()
	spell.putOnCoolDown()
}