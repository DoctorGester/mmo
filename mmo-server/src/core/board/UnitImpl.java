package core.board;

import core.board.interfaces.*;
import core.board.events.*;
import core.main.CardMaster;
import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitImpl implements Unit {

	private CardMaster owner;
	private Board board;
	private Cell position;
	private UnitData unitData;
	
	// Script unit data
	private Script script;
	private Binding unitScope;

	private String functionInit,
				   functionWalkStart,
				   functionWalkEnd,
				   functionCheckAttack,
				   functionAttackEnd,
				   functionPerformAttack,
				   functionTurnEnd,
				   functionDie;
	
	// Data being hold by unit itself, initial values are taken from UnitData
	private int maxHealth,
				currentHealth,
				attackDamage,
				bonusAttackDamage,
				maxActionPoints,
				currentActionPoints,
				restTime,
				restLeft;

	private int state = STATE_WAIT;

	private List<Spell> spells;
	private List<Passive> passives;
	private List<Buff> buffs = new ArrayList<Buff>();

	private List<HealEventListener> healEventListeners = new ArrayList<HealEventListener>();
	private List<CastEventListener> castEventListeners = new ArrayList<CastEventListener>();
	private List<AttackEventListener> attackEventListeners = new ArrayList<AttackEventListener>();
	private List<DamageEventListener> damageEventListeners = new ArrayList<DamageEventListener>();

	private Map<Buff, List<ControlType>> appliedControl = new HashMap<Buff, List<ControlType>>();

	// Temporary turn data
	private int damageDealt,
				healDone;

	public UnitImpl(CardMaster owner, UnitData unitData, Cell position){
		setOwner(owner);
		setUnitData(unitData);
		setPosition(position);
		setBoard(position.getBoard());

		callEvent(SCRIPT_EVENT_INIT);

		loadSpells();
		loadPassives();
	}

	public void addAttackEventListener(AttackEventListener listener){
		attackEventListeners.add(listener);
	}

	public void addCastEventListener(CastEventListener listener){
		castEventListeners.add(listener);
	}

	public void addDamageEventListener(DamageEventListener listener){
		damageEventListeners.add(listener);
	}

	public void addHealEventListener(HealEventListener listener){
		healEventListeners.add(listener);
	}

	public void removeAttackEventListener(AttackEventListener listener){
		attackEventListeners.remove(listener);
	}

	public void removeCastEventListener(CastEventListener listener){
		castEventListeners.remove(listener);
	}

	public void removeDamageEventListener(DamageEventListener listener){
		damageEventListeners.remove(listener);
	}

	public void removeHealEventListener(HealEventListener listener){
		healEventListeners.remove(listener);
	}

	public List<Buff> getBuffs() {
		return buffs;
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

	public Script getScript() {
		return script;
	}

	public List<Spell> getSpells() {
		return spells;
	}

	public Binding getUnitScope() {
		return unitScope;
	}

	private void loadSpells(){
		spells = new ArrayList<Spell>();

		for(String id: unitData.getSpells()){
			SpellData data = Program.getInstance().getSpellDataById(id);
			spells.add(new SpellImpl(data, this, board));
		}
	}

	private void loadPassives(){
		passives = new ArrayList<Passive>();

		for(String id: unitData.getPassives()){
			PassiveData data = Program.getInstance().getPassiveDataById(id);
			PassiveImpl passive = new PassiveImpl(data, this, board);
			passives.add(passive);
			passive.update();
		}
	}

	public void calculateTurnParameters(){
		currentHealth = currentHealth + healDone - damageDealt;
		currentHealth = Math.max(0, Math.min(currentHealth, maxHealth));

		damageDealt = 0;
		healDone = 0;

		if (state != STATE_DEAD)
			currentActionPoints = maxActionPoints;

		if (restLeft > 0){
			--restLeft;
			if (restLeft == 0 && state != STATE_DEAD)
				setState(STATE_WAIT);
		}

		if (currentHealth == 0 && state != STATE_DEAD){
			setState(STATE_DEAD);
			callEvent(SCRIPT_EVENT_DEATH);
			board.unitDies(this);
		}

		for (Spell spell: spells)
			spell.updateCoolDown();

		for (Passive passive: passives)
			passive.update();
	}

	public void doDamage(int damage, DamageType type){
		if (state != Unit.STATE_DEAD) {
			DamageEventContext context = new DamageEventContext(this, damage, type);

			for (DamageEventListener listener: damageEventListeners)
				listener.onDamageTaken(context);

			damageDealt += context.getResultDamage();
		}
	}
	
	public void doHeal(int heal){
		if (state != Unit.STATE_DEAD) {
			HealEventContext context = new HealEventContext(this, heal);

			for (HealEventListener listener: healEventListeners)
				listener.onHeal(context);

			healDone += context.getResultHealing();
		}
	}

	public Board getBoard() {
		return board;
	}

	private static Object[] concatenate(Object[] first, Object[] second) {
		int aLen = first.length;
		int bLen = second.length;
		Object[] result = new Object[aLen + bLen];
		System.arraycopy(first, 0, result, 0, aLen);
		System.arraycopy(second, 0, result, aLen, bLen);
		return result;
	}

	public Object callEvent(int event, Object ... args){
		args = concatenate(new Object[] { this, board }, args);
		switch (event){
			case SCRIPT_EVENT_INIT:
				return callFunction(functionInit, args);
			case SCRIPT_EVENT_WALK_START:
				return callFunction(functionWalkStart, args);
			case SCRIPT_EVENT_WALK_END:
				return callFunction(functionWalkEnd, args);
			case SCRIPT_EVENT_PERFORM_ATTACK:
				return callFunction(functionPerformAttack, args);
			case SCRIPT_EVENT_ATTACK_END:
				return callFunction(functionAttackEnd, args);
			case SCRIPT_EVENT_TURN_END:
				return callFunction(functionTurnEnd, args);
			case SCRIPT_EVENT_DEATH:
				return callFunction(functionDie, args);
			case SCRIPT_EVENT_CHECK_ATTACK:
				return callFunction(functionCheckAttack, args);
		}
		return null;
	}

	public boolean castSpell(int number, Cell target){
		if (number > spells.size() || number < 0)
			return false;

		Spell toCast = spells.get(number);

		if (toCast.onCoolDown())
			return false;

		// Checking targets
		SpellData spellData = toCast.getSpellData();
		switch (target.getContentsType()){
			case Cell.CONTENTS_DOODAD:
				return false;
			case Cell.CONTENTS_EMPTY:{
				if (!spellData.targetAllowed(SpellTarget.GROUND))
					return false;
			}
			case Cell.CONTENTS_UNIT:{
				if (target == position && !spellData.targetAllowed(SpellTarget.SELF))
					return false;
				else if (target != position && !spellData.targetAllowed(SpellTarget.UNIT))
					return false;
			}
		}

		// Checking spell through script and executing it if possible
		boolean result = (Boolean) toCast.callEvent(Spell.SCRIPT_EVENT_CHECK, target);
		if (result){
			owner.setUsedUnit(this);
			toCast.callEvent(Spell.SCRIPT_EVENT_CAST, target);
		}
		return result;
	}

	private Object callFunction(String function, Object ... args){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return null;
		return script.invokeMethod(function, args);
	}

	private void initFunctions(){
		functionInit = "onInit";
		functionDie = "onDeath";
		functionCheckAttack = "onCheckAttack";
		functionPerformAttack = "onAttackBegin";
		functionAttackEnd = "onAttackEnd";
		functionTurnEnd = "onTurnEnd";
		functionWalkStart = "onWalkStart";
		functionWalkEnd = "onWalkEnd";
	}
	
	private void setBoard(Board board){
		this.board = board;
		board.addUnit(this);

		unitScope = new Binding();
		unitScope.setVariable("unit", this);
		unitScope.setVariable("board", board);

		script = unitData.compileScript(unitScope);

		initFunctions();
	}

	public Cell getPosition() {
		return position;
	}

	public void setPosition(Cell position) {
		if (this.position == position)
			return;

		position.setContentsType(Cell.CONTENTS_UNIT);
		position.setUnit(this);
		
		if (this.position != null)
			this.position.setContentsType(Cell.CONTENTS_EMPTY);
		this.position = position;
		
		if (this.board != null && this.board != position.getBoard()){
			this.board.removeUnit(this);
			setBoard(position.getBoard());
		}
	}

	public UnitData getUnitData() {
		return unitData;
	}

	public void setUnitData(UnitData unitData) {
		this.unitData = unitData;
		
		setMaxActionPoints(unitData.getActionPoints());
		setAttackDamage(unitData.getDamage());
		setRestTime(unitData.getRestTime());
		setMaxHealth(unitData.getHealth());
		
		setCurrentActionPoints(getMaxActionPoints());
		setCurrentHealth(getMaxHealth());
	}

	public int getContextAttackDamage(Cell target){
		AttackEventContext context = new AttackEventContext(this, target);

		for (AttackEventListener listener: attackEventListeners)
			listener.onAttack(context);

		return context.getResultDamage();
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

	public void setBonusAttackDamage(int bonusAttackDamage) {
		this.bonusAttackDamage = bonusAttackDamage;
	}

	public int getTotalAttackDamage(){
		return getAttackDamage() + getBonusAttackDamage();
	}

	public int getMaxActionPoints() {
		return maxActionPoints;
	}

	public void setMaxActionPoints(int maxActionPoints) {
		this.maxActionPoints = maxActionPoints;
		if (currentActionPoints < maxActionPoints)
			currentActionPoints = maxActionPoints;
	}

	public int getCurrentActionPoints() {
		return currentActionPoints;
	}

	public void setCurrentActionPoints(int currentActionPoints) {
		this.currentActionPoints = currentActionPoints;
	}

	public int getDamageDealt() {
		return damageDealt;
	}

	public int getHealDone() {
		return healDone;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
		if (state == STATE_REST)
			restLeft = restTime;

		if (state == STATE_DEAD){
			currentActionPoints = 0;
			currentHealth = 0;
		}
	}

	public CardMaster getOwner() {
		return owner;
	}

	public void setOwner(CardMaster owner) {
		this.owner = owner;
	}
}
