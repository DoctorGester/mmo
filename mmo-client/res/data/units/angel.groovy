import core.board.ClientUnit
import shared.board.Board
import shared.board.Cell
import shared.board.Unit
import shared.board.DamageType
import shared.other.DataUtil

def animationStand(ClientUnit unit){
	unit.setAnimation("stand", 1.2f, true)
}

def onInit(ClientUnit unit, Board board){
	animationStand(unit)
}

def onCheckAttack(Unit unit, Board board, Unit target, Cell attackerPos){
    if (target.getState() == Unit.STATE_DEAD || target == unit)
        return false

    def targetPos = target.getPosition();

    def length = 100I;
    if (attackerPos.getX() == targetPos.getX())
        length = Math.abs(attackerPos.getY() - targetPos.getY());
    else if (attackerPos.getY() == targetPos.getY())
        length = Math.abs(attackerPos.getX() - targetPos.getX());

    if (length > 3I)
        return false

    return true
}

def onCheckAOE(Unit unit, Board board, Unit target, Cell toCheck){
    Cell pos = unit.getPosition()
    Cell tar = target.getPosition()
    def dist = DataUtil.distance(pos, toCheck);

    def tx = tar.getX()
    def px = pos.getX()
    def ty = tar.getY()
    def py = pos.getY()
    def cx = toCheck.getX()
    def cy = toCheck.getY()

    // Checking 3 conditions:
    // 1. Distance is right
    // 2. tar and toCheck are on the one side relatively to pos
    // 3. tar, pos and toCheck belong to one line
    (dist <= 3
        && ((Math.signum(tx - px) != 0 && Math.signum(tx - px) == Math.signum(cx - px))
         || (Math.signum(ty - py) != 0 && Math.signum(ty - py) == Math.signum(cy - py)))
        && ((tx == cx && px == tx)
         || (ty == cy && py == ty)))
}

def onWalkStart(ClientUnit unit, Board board){
	unit.setAnimation("walk", 1.7f, true)
}

def onWalkEnd(ClientUnit unit, Board board){
	animationStand(unit);
}

def onDeath(ClientUnit unit, Board board){
	unit.setAnimation("death", 1.2f, false)
}

def onAttackBegin(ClientUnit unit, Board board, ClientUnit target, int damage){
	Cell attackerPos = unit.getPosition();
	Cell targetPos = target.getPosition();
	
	if (attackerPos.getX() == targetPos.getX()){
		int signum = Math.signum(targetPos.getY() - attackerPos.getY())
		int start = attackerPos.getY()
		for(int y = start + signum; y != start + signum * 4; y += signum){
			if (y == attackerPos.getY())
				continue
			Cell cell = board.getCellChecked(attackerPos.getX(), y)
			if (cell == null || cell.getContentsType() != Cell.CONTENTS_UNIT)
				continue
			cell.getUnit().doDamage(damage, DamageType.ATTACK)
		}
	}
	if (attackerPos.getY() == targetPos.getY()){
		int signum = Math.signum(targetPos.getX() - attackerPos.getX());
		int start = attackerPos.getX();
		for(int x = start; x != start + signum * 4; x += signum){
			if (x == attackerPos.getX())
				continue;
			Cell cell = board.getCellChecked(x, attackerPos.getY())
			if (cell == null || cell.getContentsType() != Cell.CONTENTS_UNIT)
				continue
			cell.getUnit().doDamage(damage, DamageType.ATTACK)
		}
	}
	
	unit.setFacing(target)
	unit.setAnimation("attack", 1f, false)
	return 1.2f
}

def onAttackEnd(ClientUnit unit, Board board, Unit target){
	animationStand(unit);
	board.nextTurn();
}
