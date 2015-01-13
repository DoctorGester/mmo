package core.board.turns;

import core.board.Board;
import core.board.Cell;
import core.board.Path;
import core.board.Unit;

/**
 * @author doc
 */
public class TurnSmart implements Turn {
	public static final float DEFAULT_MOVING_SPEED = 2f;
	public static final float MOVING_SPEED = 1.5f;

	private Board board;
	private final Cell from;
	private final Cell to;
	private Unit ordered;

	private boolean finished;
	private boolean attack;

	// Moving data
	private Path path;
	private Cell pathCells[];
	private int pathPositionCurrent;

	private float waitForAttackTime;

	public TurnSmart(Board board, Cell from, Cell to){
		this.board = board;
		this.from = from;
		this.to = to;
	}

	public void execute(int mode) {
		switch (mode){
			case MODE_FIRST_STEP:{
				ordered = from.getUnit();
				board.getCurrentTurningPlayer().setUsedUnit(ordered);
				if (to.getContentsType() == Cell.CONTENTS_EMPTY){
					path = new Path(ordered.getPosition(), to, ordered.getCurrentActionPoints());
					path.find();
					pathCells = path.getPath();
					pathPositionCurrent = 0;
					ordered.updateMovingFacing(pathCells[0]);
					ordered.callEvent(Unit.SCRIPT_EVENT_WALK_START);
				} else {
					waitForAttackTime = ((Number) ordered.callEvent(Unit.SCRIPT_EVENT_PERFORM_ATTACK, to.getUnit(), ordered.getContextAttackDamage(to))).floatValue();
					attack = true;
				}
				break;
			}
			case MODE_LAST_STEP:{
				if (attack){
					ordered.callEvent(Unit.SCRIPT_EVENT_ATTACK_END, to.getUnit());
				}
				break;
			}
		}
	}

	public void update(float tpf) {
		ordered.updateFacing();
		if (to.getContentsType() == Cell.CONTENTS_EMPTY){
			updateMoving();
		} else {
			waitForAttackTime -= tpf;
			if (waitForAttackTime <= 0)
				finished = true;
		}
	}

	public boolean hasLastStep() {
		return true;
	}

	public boolean firstStepFinished(){
		return finished;
	}

	public String toStringRepresentation() {
		if (attack)
			return ordered.getOwner().getName()
					+ " uses "
					+ ordered.getUnitData().getName()
					+ " to attack "
					+ to.getUnit().getUnitData().getName();
		else
			return ordered.getOwner().getName()
					+ " moves "
					+ ordered.getUnitData().getName();
	}

	private boolean moveTowards(int tx, int ty){
		float dx = tx - ordered.getRealPositionX(),
			  dy = ty - ordered.getRealPositionY(),
			  len = (float) Math.sqrt(dx * dx + dy * dy);

		if (len <= MOVING_SPEED)
			return true;

		float vx = dx / len * MOVING_SPEED,
				vy = dy / len * MOVING_SPEED;

		ordered.setRealPositionX(ordered.getRealPositionX() + vx);
		ordered.setRealPositionY(ordered.getRealPositionY() + vy);
		return false;
	}

	private void updateMoving(){
		if (finished)
			return;
		Cell next = pathCells[pathPositionCurrent];
		int x = next.getX() * Cell.CELL_WIDTH,
			y = next.getY() * Cell.CELL_HEIGHT;
		boolean targetReached = moveTowards(x, y);
		if (targetReached){
			if (pathPositionCurrent > 0)
				ordered.setCurrentActionPoints(ordered.getCurrentActionPoints() - 1);
			ordered.setPosition(next);
			pathPositionCurrent++;
			if (pathPositionCurrent >= path.getLength()){
				ordered.callEvent(Unit.SCRIPT_EVENT_WALK_END);
				finished = true;
			} else {
				ordered.updateMovingFacing(pathCells[pathPositionCurrent]);
			}
			updateMoving();
		}
	}

	@Override
	public String toString() {
		return super.toString() + "{" +
				"from=" + from +
				", to=" + to +
				'}';
	}
}
