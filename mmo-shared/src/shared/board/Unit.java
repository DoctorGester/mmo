package shared.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import shared.board.data.UnitData;
import shared.map.CardMaster;

import java.util.List;

/**
 * @author doc
 */
public interface Unit {
	public static final int STATE_WAIT = 0x00,
							STATE_DEAD = 0x02,
							STATE_REST = 0x03;

	public static final int SCRIPT_EVENT_INIT = 0x00,
							SCRIPT_EVENT_WALK_START = 0x01,
							SCRIPT_EVENT_WALK_END = 0x02,
							SCRIPT_EVENT_CHECK_ATTACK = 0x03,
							SCRIPT_EVENT_PERFORM_ATTACK = 0x04,
							SCRIPT_EVENT_ATTACK_END = 0x05,
							SCRIPT_EVENT_TURN_END = 0x06,
							SCRIPT_EVENT_DEATH = 0x07,
							SCRIPT_EVENT_CHECK_AOE = 0x08;

	public void calculateTurnParameters();
	public Object callEvent(int event, Object ... args);
	public boolean castSpell(int number, Cell target);

	public void doDamage(int damage, DamageType type);
	public void doHeal(int heal);

	public Board getBoard();
	public Script getScript();
	public Cell getPosition();
	public CardMaster getOwner();
	public UnitData getUnitData();
	public Binding getUnitScope();
	public List<? extends Ability> getAbilities();
	public List<? extends Buff> getBuffs();

	public int getState();
	public int getRestTime();
	public int getRestLeft();
	public int getMaxHealth();
	public int getCurrentHealth();
	public int getAttackDamage();
	public int getBonusAttackDamage();
	public int getTotalAttackDamage();
	public int getMaxActionPoints();
	public int getCurrentActionPoints();

	public int getDamageDealt();
	public int getHealDone();

	public void addBuff(Buff buff);
	public void purgeBuffs();
	public boolean hasBuff(String id);
	public boolean hasBuff(Buff buff);
	public boolean removeBuff(Buff buff);

	public void setPosition(Cell cell);

	public void setState(int state);
	public void setRestTime(int restTime);
	public void setMaxHealth(int maxHealth);
	public void setRestLeft(int restLeft);
	public void setMaxActionPoints(int maxActionPoints);
	public void setCurrentHealth(int currentHealth);
	public void setAttackDamage(int attackDamage);
	public void setBonusAttackDamage(int bonusAttackDamage);
	public void setCurrentActionPoints(int currentActionPoints);

	public void applyControl(ControlType type, Buff buff);
	public boolean controlApplied(ControlType type);
}
