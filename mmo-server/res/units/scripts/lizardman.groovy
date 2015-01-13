import core.board.DamageType
import core.board.interfaces.Board
import core.board.interfaces.Cell
import core.board.interfaces.Unit

def onCheckAttack(Unit unit, Board board, Unit target, Cell from){
	if (target.getState() == Unit.STATE_DEAD || target == unit)
		return false

	def targetPos = target.getPosition();
	
	return (Math.abs(from.getX() - targetPos.getX()) + Math.abs(from.getY() - targetPos.getY()) == 1I)
}

def onAttackBegin(Unit unit, Board board, Unit target, int damage){
	def attackerPos = unit.getPosition();
	def targetPos = target.getPosition();
	
	target.doDamage(damage, DamageType.ATTACK);
	
	return 0.5F;
}

def onAttackEnd(Unit unit, Board board, Unit target){
	board.nextTurn();
}
