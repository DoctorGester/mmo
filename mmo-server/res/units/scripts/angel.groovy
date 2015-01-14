import shared.board.Board
import shared.board.Cell
import shared.board.DamageType
import shared.board.Unit

def onInit(Unit unit, Board board){
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

def onAttackBegin(Unit unit, Board board, Unit target, int damage){
    def attackerPos = unit.getPosition();
    def targetPos = target.getPosition();

    if (attackerPos.getX() == targetPos.getX()){
        def signum = Math.signum(targetPos.getY() - attackerPos.getY());
        def start = attackerPos.getY();
        for(int y = start + signum; y != start + signum * 4; y += signum){
            if (y == attackerPos.getY())
                continue;
            def cell = board.getCellChecked(attackerPos.getX(), y);
            if (cell == null || cell.getContentsType() != Cell.CONTENTS_UNIT)
                continue;
            cell.getUnit().doDamage(damage, DamageType.ATTACK);
        }
    }
    if (attackerPos.getY() == targetPos.getY()){
        def signum = Math.signum(targetPos.getX() - attackerPos.getX());
        def start = attackerPos.getX();
        for(int x = start; x != start + signum * 4; x += signum){
            if (x == attackerPos.getX())
                continue;
            def cell = board.getCellChecked(x, attackerPos.getY());
            if (cell == null || cell.getContentsType() != Cell.CONTENTS_UNIT)
                continue;
            cell.getUnit().doDamage(damage, DamageType.ATTACK);
        }
    }

    return 0.5F;
}

def onAttackEnd(Unit unit, Board board, Unit target){
    board.nextTurn();
}
