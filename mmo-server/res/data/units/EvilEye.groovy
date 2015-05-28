import shared.board.Board
import shared.board.Cell
import shared.board.DamageType
import shared.board.Unit

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
