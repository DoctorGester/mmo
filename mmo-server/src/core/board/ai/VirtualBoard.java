package core.board.ai;

import core.board.Alliance;
import core.board.BoardImpl;
import core.board.UnitData;
import core.board.interfaces.Board;
import core.board.BuffData;
import core.board.interfaces.Cell;
import core.board.interfaces.Spell;
import core.board.interfaces.Unit;
import core.main.CardMaster;
import core.main.inventory.items.CardItem;
import program.main.Program;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class VirtualBoard implements Board {
	protected AI ai;
	protected Board board;

	protected VirtualCell cells[];
	protected List<VirtualUnit> units;
	protected List<VirtualBuff> buffs;

	private VirtualTurn turn;

	private int id;
	private int width, height;

	public VirtualBoard(AI ai, int width, int height){
		this.ai = ai;

		units = new ArrayList<VirtualUnit>();
		buffs = new ArrayList<VirtualBuff>();

		this.width = width;
		this.height = height;

		cells = new VirtualCell[width * height];

		// Filling cell array with actual empty cells
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				cells[y * width + x] = new VirtualCell(this, x, y);
	}

	public void snapshot(Board board){
		if (board != this.board)
			units = new ArrayList<VirtualUnit>(board.getUnits().size());

		buffs.clear();

		setId(board.getId());

		// TODO consider doodads please
		// Just clearing all cells
		for(int x = 0; x < board.getWidth(); x++)
			for(int y = 0; y < board.getHeight(); y++){
				VirtualCell cell = getCell(x, y);
				cell.setUnit(null);
				cell.setDoodad(null);
				cell.setContentsType(Cell.CONTENTS_EMPTY);

				cell.weight = 0;
			}

		// They are most likely to be strictly ordered
		int index = 0;
		for(Unit unit: board.getUnits()){
			VirtualUnit virtualUnit;
			if (index >= units.size()){
				virtualUnit = new VirtualUnit(this);
				units.add(index, virtualUnit);
			} else {
				virtualUnit = units.get(index);
			}
			virtualUnit.snapshot(unit);
			index++;
		}

		this.board = board;
	}

	public void nextTurn(){
		for(Iterator<VirtualBuff> iterator = buffs.iterator(); iterator.hasNext(); ){
			VirtualBuff buff = iterator.next();
			buff.update();
			if (buff.hasEnded())
				iterator.remove();
		}

		for (VirtualUnit u: units){
			u.calculateTurnParameters();
			u.callFunction("onTurnEnd", u, this);
		}
	}

	public List<Unit> getUnits(){
		return new ArrayList<Unit>(units);
	}

	public List<CardMaster> getCardMasters() {
		return null;
	}

	public boolean areAllies(CardMaster... cardMasters) {
		return board.areAllies(cardMasters);
	}

	public List<CardItem> getPickedCards(CardMaster cardMaster) {
		return new LinkedList<CardItem>();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public VirtualCell getCell(int x, int y) {
		return cells[y * width + x];
	}

	public VirtualCell getCellChecked(int x, int y){
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;
		return cells[y * width + x];
	}

	public VirtualBuff addBuff(String id, int timesToRepeat, int period, int initialDelay, Object data){
		BuffData buffData = Program.getInstance().getBuffScriptById(id);
		VirtualBuff buff = new VirtualBuff(this, buffData, timesToRepeat, period, initialDelay, data);
		buffs.add(buff);
		return buff;
	}

	public VirtualBuff addBuff(String id, int timesToRepeat, int period, Object data){
		return addBuff(id, timesToRepeat, period, period, data);
	}

	public VirtualBuff addBuff(String id, int timesToRepeat, int period){
		return addBuff(id, timesToRepeat, period, period);
	}

	public int getState(){
		return board.getState();
	}

	public short getTurnNumber() {
		return 0;
	}

	public CardMaster getCurrentTurningPlayer() {
		return null;
	}

	public void setPlacementArea(Rectangle[] area) {}
	public void setTimeRemaining(float time) {}
	public void setTurnTime(float time) {}
	public void selectTurningPlayer() {}
	public void update(float tpf) {}
	public void addCardMaster(CardMaster cardMaster) {}
	public void playerFinishedPlacement(CardMaster cardMaster) {}
	public void addAlliance(Alliance alliance) {}
	public void addUnit(Unit unit) {}
	public void removeUnit(Unit unit) {}
	public void unitDies(Unit unit) {}
	public void checkGameOver() {}
	public void skipTurn() {}

	public Unit handlePickOrder(CardMaster cardMaster, CardItem card, UnitData unitData) {
		return null;
	}

	public boolean handlePlacementOrder(CardMaster owner, Cell selected, Cell order) {
		return false;
	}

	public boolean handleCastOrder(CardMaster owner, Cell selected, Cell target, int spell) {
		return false;
	}

	public boolean handleSimpleOrder(CardMaster owner, Cell selected, Cell order) {
		return false;
	}

	public boolean handleCastCardSpellOrder(CardMaster caster, int cardId) {
		return false;
	}

	public int computeHash(){
		int result = 0;

		for (Unit u: units){
			result += u.getCurrentHealth()
					+ u.getState()
					+ u.getAttackDamage()
					+ u.getOwner().getBattleId()
					+ u.getCurrentActionPoints()
					+ u.getUnitData().getId()
					+ u.getPosition().getX()
					+ u.getPosition().getY()
					+ u.getBonusAttackDamage()
					+ u.getDamageDealt()
					+ u.getHealDone();

			for (Spell spell: u.getSpells())
				result += spell.getCoolDownLeft() +
						spell.getSpellData().getId().hashCode();
		}

		result +=  board.getCurrentTurningPlayer().getBattleId();

		return result;
	}
}
