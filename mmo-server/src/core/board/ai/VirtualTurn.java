package core.board.ai;

import core.board.ServerBoard;
import core.board.TurnManager;
import shared.board.Cell;
import shared.map.CardMaster;

/**
 * @author doc
 */
public class VirtualTurn implements Comparable<VirtualTurn> {

	public static final int TURN_MOVE = 0x00,
							TURN_ATTACK = 0x01,
							TURN_CAST = 0x02;

	private int type;
	private float weight;
	private VirtualBoard board;
	private VirtualUnit executor;
	private VirtualCell moveToExecute;
	private VirtualCell target;
	private VirtualAbility spell;

	public VirtualTurn(VirtualBoard board, VirtualUnit executor) {
		this.board = board;
		this.executor = executor;
	}

	public boolean execute(){
		TurnManager turnManager = TurnManager.getInstance();
		ServerBoard realBoard = board.ai.realBoard;
		CardMaster cardMaster = board.ai.cardMaster;

		Cell target = realBoard.getCell(this.target.getX(), this.target.getY());
		//System.out.println(board.ai.realBoard.getTurnNumber() + "# " + executor.getOwner().getBattleId() + " EXECUTE (" + weight + ") TYPE (" + type + ") " + target.getX() + " " + target.getY() + " from " + executor.getUnit().getPosition().getX() + " " + executor.getUnit().getPosition().getY()  + " " + board.ai.realBoard.getUnits().get(0).getCurrentHealth());
		switch (type){
			case TURN_MOVE:{
				return turnManager.smart(realBoard, cardMaster, executor.getUnit().getPosition(), target);
			}
			case TURN_ATTACK:{
				if (executor.getPosition() == moveToExecute){
					return turnManager.smart(realBoard, cardMaster, executor.getUnit().getPosition(), target);
				} else {
					Cell move = realBoard.getCell(moveToExecute.getX(), moveToExecute.getY());
					if (turnManager.smart(realBoard, cardMaster, executor.getUnit().getPosition(), move))
						return turnManager.smart(realBoard, cardMaster, executor.getUnit().getPosition(), target);
					return false;
				}
			}
			case TURN_CAST:{
				int spell = executor.getSpellNumber(this.spell);
				if (executor.getPosition() == moveToExecute){
					return turnManager.cast(realBoard, cardMaster, spell, executor.getUnit().getPosition(), target);
				} else {
					Cell move = realBoard.getCell(moveToExecute.getX(), moveToExecute.getY());
					if (turnManager.smart(realBoard, cardMaster, executor.getUnit().getPosition(), move))
						return turnManager.cast(realBoard, cardMaster, spell, executor.getUnit().getPosition(), target);
					return false;
				}
			}
		}
		return false;
	}

	public void setMoveToExecute(VirtualCell moveToExecute) {
		this.moveToExecute = moveToExecute;
	}

	public void setSpell(VirtualAbility spell) {
		this.spell = spell;
	}

	public void setTarget(VirtualCell target) {
		this.target = target;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public void modWeight(float delta){
		weight += delta;
	}

	@Override
	public int compareTo(VirtualTurn to) {
		return Float.compare(to.weight, weight);
	}

	public String toString(){
		return super.toString() + " [" + weight + "]";
	}
}
