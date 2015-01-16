package core.board;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import core.main.Client;
import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import shared.board.*;
import shared.board.data.PassiveData;
import shared.board.data.SpellData;
import shared.board.data.UnitData;
import shared.board.events.*;
import shared.map.CardMaster;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUnit implements Unit {
	public static final int STATE_WAIT = 0x00,
					  		STATE_DEAD = 0x02,
							STATE_REST = 0x03;

	private static final float DEFAULT_MOVING_SPEED = 2f;
	
	private float realPositionX,
				  realPositionY;

	private Vector2f facingFrom = new Vector2f(),
					 facingTo = new Vector2f(),
					 facingCurrent = new Vector2f();

	private float facingStep = 1f;

	private String animationName = "";
	private boolean animationLooped;
	private float animationSpeed;
	
	private CardMaster owner;
	private ClientBoard board;
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
				   functionDie,
				   functionCheckAOE;

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

	private List<ClientSpell> spells;
	private List<Passive> passives;
	private List<ClientBuff> buffs = new ArrayList<ClientBuff>();
	private Map<Buff, List<ControlType>> appliedControl = new HashMap<Buff, List<ControlType>>();

	private List<HealEventListener> healEventListeners = new ArrayList<HealEventListener>();
	private List<CastEventListener> castEventListeners = new ArrayList<CastEventListener>();
	private List<AttackEventListener> attackEventListeners = new ArrayList<AttackEventListener>();
	private List<DamageEventListener> damageEventListeners = new ArrayList<DamageEventListener>();

	// Temporary turn data
	private int damageDealt,
				healDone;

	// Another path data, only for visual representation
	private Wave wave, possibleWave;

	public ClientUnit(CardMaster owner, UnitData unitData, ClientCell position){
		setOwner(owner);
		setUnitData(unitData);
		setBoard(position.getBoard());
		setPosition(position);

		loadSpells();
		loadPassives();

		setFacingInstantly(FastMath.HALF_PI);

		callEvent(SCRIPT_EVENT_INIT);
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

	public List<ClientBuff> getBuffs() {
		return buffs;
	}

	public void purgeBuffs(){
		List<Buff> buffs = new ArrayList<Buff>(this.buffs);
		for (Buff buff: buffs)
			buff.end();
	}

	public boolean hasBuff(String id) {
		for (Buff buff: buffs)
			if (buff.getBuffData().getId().equals(id))
				return true;

		return false;
	}

	public boolean hasBuff(Buff buff) {
		return buff instanceof ClientBuff && buffs.contains(buff);
	}

	public void addBuff(Buff buff){
		buffs.add((ClientBuff) buff);
	}

	public boolean removeBuff(Buff buff){
		appliedControl.remove(buff);
		return buff instanceof ClientBuff && buffs.remove(buff);
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

	public List<ClientSpell> getSpells() {
		return spells;
	}

	public Binding getUnitScope() {
		return unitScope;
	}

	private void loadSpells(){
		spells = new ArrayList<ClientSpell>();

		for(String id: unitData.getSpells()){
			SpellData data = Program.getInstance().getSpellDataById(id);
			spells.add(new ClientSpell(data, this, board));
		}
	}

	private void loadPassives(){
		passives = new ArrayList<Passive>();

		for (String id: unitData.getPassives()){
			PassiveData data = Program.getInstance().getPassiveDataById(id);
			Passive passive = new ClientPassive(data, this, board);
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
		}

		for (Spell spell: spells)
			spell.updateCoolDown();
	}

	public TurnResults getTurnResults(){
		return new TurnResults(this, damageDealt, healDone);
	}

	private void tryToCalculateWavePath() {
		if (currentActionPoints == 0){
			wave = null;
			return;
		}
		wave = new Wave(position, currentActionPoints);
		wave.calculate();
	}

	private void tryToCalculatePossibleWavePath(){
		if (maxActionPoints == 0){
			possibleWave = null;
			return;
		}
		possibleWave = new Wave(position, maxActionPoints);
		possibleWave.calculate();
	}

	public void resetWavePath(){
		wave = null;
		possibleWave = null;
	}

	public void updateMovingFacing(Cell next){
		Vector2f moving = new Vector2f(next.getX() * Cell.CELL_WIDTH, next.getY() * Cell.CELL_HEIGHT);
		if (moving.lengthSquared() == 0)
			return;
		moving.subtractLocal(getRealPositionX(), getRealPositionY());
		moving.normalizeLocal();
		setFacing(moving);
	}

	public void updateFacing(){
		facingStep = FastMath.clamp(facingStep + 0.1f, 0f, 1f);
		facingCurrent.interpolate(facingFrom, facingTo, facingStep);
	}

	public boolean canMoveTo(Cell cell){
		if (wave == null)
			tryToCalculateWavePath();

		return wave != null && wave.getPathing(cell) != -1 && !controlApplied(ControlType.ROOT) && !controlApplied(ControlType.STUN);
	}

	public boolean canPossiblyMoveTo(Cell cell){
		if (possibleWave == null)
			tryToCalculatePossibleWavePath();

		return possibleWave != null && possibleWave.getPathing(cell) != -1;
	}

	public void setFacing(Vector2f facing){
		facingTo.set(facing);
		facingFrom.set(facingCurrent);
		facingStep = 0f;
	}

	public void setFacing(float facing){
		setFacing(new Vector2f(FastMath.cos(facing), FastMath.sin(facing)));
	}

	public void setFacing(ClientUnit to){
		if (to == this)
			return;

		setFacing(new Vector2f(to.getRealPositionX(), to.getRealPositionY())
					  .subtractLocal(getRealPositionX(), getRealPositionY())
					  .normalizeLocal());
	}

	public void setFacing(Cell to){
		if (to == position)
			return;

		setFacing(new Vector2f(to.getX(), to.getY())
				.subtractLocal(position.getX(), position.getY())
				.normalizeLocal());
	}

	public void setFacingInstantly(Vector2f facing){
		facingTo.set(facing);
		facingFrom.set(facing);
		facingCurrent.set(facing);
		facingStep = 1f;
	}

	public void setFacingInstantly(float facing){
		setFacingInstantly(new Vector2f(FastMath.cos(facing), FastMath.sin(facing)));
	}

	public Vector2f getFacing(){
		return facingCurrent;
	}

	public String getAnimationName() {
		return animationName;
	}

	public void setAnimation(String animationName, float speed, boolean loop){
		this.animationName = animationName;
		this.animationLooped = loop;
		this.animationSpeed = speed;
	}

	public boolean isAnimationLooped() {
		return animationLooped;
	}

	public float getAnimationSpeed() {
		return animationSpeed;
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
			case SCRIPT_EVENT_CHECK_AOE:
				return callFunction(functionCheckAOE, args);
		}
		return null;
	}

	// TODO look carefully into that
	public boolean castSpell(int number, Cell target) {
		throw new NotImplementedException();
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
		functionCheckAOE = "onCheckAOE";
		functionPerformAttack = "onAttackBegin";
		functionAttackEnd = "onAttackEnd";
		functionTurnEnd = "onTurnEnd";
		functionWalkStart = "onWalkStart";
		functionWalkEnd = "onWalkEnd";
	}

	private void setBoard(ClientBoard board){
		this.board = board;
		board.addUnit(this);

		unitScope = new Binding();

		script = unitData.compileScript(Program.getInstance().getScriptEngine(), unitScope);

		initFunctions();
	}

	public Cell getPosition() {
		return position;
	}

	public void swapPositionWith(ClientUnit unit){
		Cell to = unit.getPosition();

		to.setUnit(this);
		position.setUnit(unit);

		unit.position = this.position;
		this.position = to;

		this.realPositionFromCell();
		unit.realPositionFromCell();
	}

	private void realPositionFromCell(){
		setRealPositionX(position.getX() * Cell.CELL_WIDTH);
		setRealPositionY(position.getY() * Cell.CELL_HEIGHT);
	}

	public void setPosition(Cell cell) {
		ClientCell position = (ClientCell) cell;

		if (this.position == position)
			return;

		Cell last = this.position;

		position.setContentsType(Cell.CONTENTS_UNIT);
		position.setUnit(this);
		
		if (this.position != null)
			this.position.setContentsType(Cell.CONTENTS_EMPTY);
		this.position = position;
		
		if (this.board != null && this.board != position.getBoard()){
			this.board.removeUnit(this);
			setBoard(position.getBoard());
		}

		realPositionFromCell();

		if (last != this.position){
			for (ClientUnit unit: board.getUnits()){
				unit.tryToCalculateWavePath();
				unit.tryToCalculatePossibleWavePath();
			}
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


	public void fireCastEvent(Cell target, Spell spell) {
		CastEventContext context = new CastEventContext(this, target, spell);

		for (CastEventListener listener: castEventListeners)
			listener.onCast(context);
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

	public float getRealPositionX() {
		return realPositionX;
	}

	public void setRealPositionX(float realPositionX) {
		this.realPositionX = realPositionX;
	}

	public float getRealPositionY() {
		return realPositionY;
	}

	public void setRealPositionY(float realPositionY) {
		this.realPositionY = realPositionY;
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
