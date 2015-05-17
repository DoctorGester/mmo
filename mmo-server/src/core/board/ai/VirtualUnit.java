package core.board.ai;

import groovy.lang.Binding;
import groovy.lang.Script;
import shared.board.*;
import shared.board.data.AbilityData;
import shared.board.data.UnitData;
import shared.map.CardMaster;

import java.util.*;

public class VirtualUnit implements Unit {
	private VirtualBoard virtualBoard;
	private VirtualCell position;
	private Unit unit;
	private UnitData unitData;
	private Wave wave;
	private Binding scope;
	private int id = -1;
	private int state = -1;

	private Map<Buff, List<ControlType>> appliedControl = new HashMap<Buff, List<ControlType>>();

	protected float importance = 0f;
	
	// Data being hold by unit itself, initial values are taken from UnitData
	private int maxHealth,
				currentHealth,
				attackDamage,
				bonusAttackDamage,
				maxActionPoints,
				currentActionPoints,
				restTime,
				restLeft;

	protected List<VirtualAbility> spells;
	private List<Buff> buffs = new LinkedList<Buff>();

	public VirtualUnit(VirtualBoard board){
		virtualBoard = board;
	}

	public void snapshot(Unit unit){
		this.unit = unit;
		unitData = unit.getUnitData();

		setUnitData(unitData);
		setPosition(unit.getPosition());
		setState(unit.getState());

		// TODO copy applied control (idk how to copy buffs for now)

		restLeft = unit.getRestLeft();
	}

	public int getSpellNumber(VirtualAbility spell){
		return spells.indexOf(spell);
	}

	public Unit getUnit() {
		return unit;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void doDamage(int damage, DamageType type){
		if (state == Unit.STATE_DEAD)
			return;

		boolean ally = virtualBoard.board.areAllies(getOwner(), virtualBoard.ai.cardMaster);

		if (ally && virtualBoard.ai.calculatingHeatMaps)
			return;

		float mod = ally ? -1 : 1;

		currentHealth = Math.max(0, currentHealth - damage);

		if (!virtualBoard.ai.calculatingHeatMaps)
			if (importance != 0)
				mod *= importance;
			//virtualBoard.ai.turn.modWeight(mod * importance);
		virtualBoard.ai.turn.modWeight(mod * damage);
		virtualBoard.ai.turn.modWeight((maxHealth - currentHealth) * mod);
		if (currentHealth <= 0){
			virtualBoard.ai.turn.modWeight(10 * mod);
			state = Unit.STATE_DEAD;
			callFunction("onDeath", this, virtualBoard);
		}
	}
	
	public void doHeal(int heal){
		if (state == Unit.STATE_DEAD)
			return;

		boolean ally = virtualBoard.board.areAllies(getOwner(), virtualBoard.ai.cardMaster);

		if (!ally && virtualBoard.ai.calculatingHeatMaps)
			return;

		int mod = ally ? 1 : -1;
		if (!virtualBoard.ai.calculatingHeatMaps){
			if (importance != 0)
				mod *= importance;
		}
			//virtualBoard.ai.turn.modWeight(mod * importance);
		/*if (ally){
			int mod = heal;
			if (currentHealth + mod > maxHealth){
				mod = maxHealth - currentHealth;
			}
			virtualBoard.ai.turn.modWeight(mod);
		} else {
			virtualBoard.ai.turn.modWeight(-heal * heal);
		}
		virtualBoard.ai.turn.modWeight((maxHealth - currentHealth) * pmod);*/
		if (ally && currentHealth + heal >= maxHealth)
			virtualBoard.ai.turn.modWeight((maxHealth - currentHealth) * mod);
		else
			virtualBoard.ai.turn.modWeight(heal * mod);
		currentHealth = Math.min(maxHealth, currentHealth + heal);
	}

	public void calculateTurnParameters(){
		if (restLeft > 0){
			if (--restLeft == 0)
				setState(Unit.STATE_WAIT);
		}
	}

	public Object callEvent(int event, Object... args) {
		return null;
	}

	public boolean castSpell(int number, Cell target) {
			return false;
	}

	public Script getScript() {
		return null;
	}

	public VirtualBoard getBoard() {
		return virtualBoard;
	}

	public Wave getWave() {
		return wave;
	}

	public void calculateWave(){
		wave = new Wave(virtualBoard, position, unitData.getActionPoints());
		wave.calculate();
	}

	public boolean cast(VirtualAbility toCast, VirtualCell target){
		if (toCast.onCoolDown())
			return false;

		if (state != Unit.STATE_WAIT)
			return false;

		// Checking targets
		AbilityData abilityData = toCast.getAbilityData();

		switch (target.getContentsType()){
			case Cell.CONTENTS_DOODAD:
				return false;
			case Cell.CONTENTS_EMPTY:{
				if (!abilityData.targetAllowed(AbilityTarget.GROUND))
					return false;
			}
			case Cell.CONTENTS_UNIT:{
				if (target == position && !abilityData.targetAllowed(AbilityTarget.SELF))
					return false;
				else if (target != position && !abilityData.targetAllowed(AbilityTarget.UNIT))
					return false;
			}
		}

		// Checking spell through script and executing it if possible
		boolean result = (Boolean) toCast.callEvent(Ability.SCRIPT_EVENT_CHECK, target);
		if (result)
			toCast.callEvent(Ability.SCRIPT_EVENT_CAST, target);

		return result;
	}

	public Object callFunction(String function, Object ... args){
		Script script = unit.getScript();
		Binding old = script.getBinding();
		script.setBinding(scope);
		if (script.getMetaClass().respondsTo(script, function).isEmpty()){
			script.setBinding(old);
			return null;
		}
		Object result = script.invokeMethod(function, args);
		script.setBinding(old);
		return result;
	}

	public void move(VirtualCell cell){
		callFunction("onWalkStart", this, virtualBoard);
		setPosition(cell);
		callFunction("onWalkEnd", this, virtualBoard);
	}

	public boolean attack(VirtualUnit target){
		boolean canAttack = (Boolean) callFunction("onCheckAttack", this, virtualBoard, target, position);
		if (canAttack){
			callFunction("onAttackBegin", this, virtualBoard, target, getTotalAttackDamage());
			callFunction("onAttackEnd", this, virtualBoard, target);
		}
		return canAttack;
	}

	public List<VirtualAbility> getAbilities(){
		return spells;
	}

	public List<Buff> getBuffs() {
		return buffs;
	}

	public boolean hasBuff(String id) {
		for (Buff buff: buffs)
			if (buff.getBuffData().getId().equals(id))
				return true;

		return false;
	}

	public boolean hasBuff(Buff buff) {
		return buffs.contains(buff);
	}

	public VirtualCell getPosition() {
		return position;
	}

	public void setPosition(Cell cellPosition) {
		setPosition(virtualBoard.getCell(cellPosition.getX(), cellPosition.getY()));
	}

	public void setPosition(VirtualCell cellPosition) {
		if (position != null){
			position.setUnit(null);
			position.setContentsType(Cell.CONTENTS_EMPTY);
		}

		this.position = cellPosition;
		this.position.setUnit(this);
	}

	public UnitData getUnitData() {
		return unitData;
	}

	public Binding getUnitScope() {
		return null;
	}

	public void setUnitData(UnitData unitData) {
		scope = new Binding(new HashMap<Object, Object>(unit.getUnitScope().getVariables()));
		scope.setVariable("unit", this);
		scope.setVariable("board", virtualBoard);

		spells = new ArrayList<VirtualAbility>();
		for(Ability ability : unit.getAbilities()){
			spells.add(new VirtualAbility(ability, this, virtualBoard));
		}

		this.unitData = unitData;
		
		setMaxActionPoints(unitData.getActionPoints());
		setAttackDamage(unitData.getDamage());
		setRestTime(unitData.getRestTime());
		setMaxHealth(unitData.getHealth());
		
		setCurrentActionPoints(unit.getCurrentActionPoints());
		setCurrentHealth(unit.getCurrentHealth());
	}

	public int getRestTime() {
		return restTime;
	}

	public void setRestTime(int restTime) {
		this.restTime = restTime;
	}

	public int getRestLeft() {
		return restLeft;
	}

	public void setRestLeft(int restLeft) {
		this.restLeft = restLeft;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public void setCurrentHealth(int currentHealth) {
		this.currentHealth = currentHealth;
	}

	public int getAttackDamage() {
		return attackDamage;
	}

	public void setAttackDamage(int attackDamage) {
		this.attackDamage = attackDamage;
	}

	public int getBonusAttackDamage() {
		return bonusAttackDamage;
	}

	public int getTotalAttackDamage() {
		return getAttackDamage() + getBonusAttackDamage();
	}

	public void setBonusAttackDamage(int bonusAttackDamage) {
		this.bonusAttackDamage = bonusAttackDamage;
	}

	public int getMaxActionPoints() {
		return maxActionPoints;
	}

	public void setMaxActionPoints(int maxActionPoints) {
		this.maxActionPoints = maxActionPoints;
	}

	public int getCurrentActionPoints() {
		return currentActionPoints;
	}

	public void setCurrentActionPoints(int currentActionPoints) {
		this.currentActionPoints = currentActionPoints;
	}

	public int getDamageDealt() {
		return unit.getDamageDealt();
	}

	public int getHealDone() {
		return unit.getHealDone();
	}

	public void purgeBuffs(){
		List<Buff> buffs = new ArrayList<Buff>(this.buffs);
		for (Buff buff: buffs)
			buff.end();
	}

	public void addBuff(Buff buff){
		buffs.add(buff);
	}


	public boolean removeBuff(Buff buff){
		appliedControl.remove(buff);
		return buffs.remove(buff);
	}

	public void applyControl(ControlType type, Buff buff){
		List<ControlType> buffControl = appliedControl.get(buff);

		if (buffControl == null){
			buffControl = new ArrayList<ControlType>();
			appliedControl.put(buff, buffControl);
		}

		buffControl.add(type);
	}

	public boolean controlApplied(ControlType type){
		for (List<ControlType> buffControl: appliedControl.values())
			if (buffControl.contains(type))
				return true;

		return false;
	}

	public int getState() {
		return state;
	}

	public CardMaster getOwner() {
		return unit.getOwner();
	}
}
