import shared.board.Board
import shared.board.Cell
import shared.board.Unit
import shared.board.DamageType

def onCheckAttack(Unit unit, Board board, Unit target, Cell from){
    if (target.getState() == Unit.STATE_DEAD || target == unit)
        return false

    def targetPos = target.getPosition()

    return (Math.abs(from.getX() - targetPos.getX()) + Math.abs(from.getY() - targetPos.getY()) == 1I)
}

def onCheckAOE(Unit unit, Board board, Unit target, Cell toCheck){
    target.getPosition() == toCheck
}

def onAttackBegin(Unit unit, Board board, Unit target, int damage){
	target.doDamage(damage, DamageType.ATTACK)

	unit.setFacing(target)
	unit.setAnimation("attack", 1.5f, false)

	1.0f
}

def animationStand(Unit unit){
	unit.setAnimation("stand", 1f, true)
}

def onAttackEnd(Unit unit, Board board, Unit target){
	animationStand(unit)
	board.nextTurn()
}

def onInit(Unit unit, Board board){
	animationStand(unit)
}

def onWalkStart(Unit unit, Board board){
	unit.setAnimation("walk", 1.7f, true)
}

def onWalkEnd(Unit unit, Board board){
	animationStand(unit)
}

def onDeath(Unit unit, Board board){
	unit.setAnimation("death", 1.2f, false)
}

