package core.board.ai;

import groovy.lang.Binding;
import groovy.lang.Script;
import shared.board.Cell;
import shared.board.Ability;
import shared.board.Unit;
import shared.board.data.AbilityData;

import java.util.HashMap;

public class VirtualAbility implements Ability {
	private AbilityData abilityData;
	private VirtualUnit caster;
	private VirtualBoard board;

	private Ability ability;
	private Script script;
	private Binding scope;

	private String functionCheck, functionCast;

	private int coolDownLeft;

	public VirtualAbility(Ability ability, VirtualUnit caster, VirtualBoard board) {
		this.ability = ability;
		this.abilityData = ability.getAbilityData();
		this.caster = caster;
		this.board = board;
		this.coolDownLeft = ability.getCoolDownLeft();

		initScope();
	}

	private Object callFunction(String function, VirtualCell target){
		Binding old = script.getBinding();
		script.setBinding(scope);
		Object result = script.invokeMethod(function, new Object[] { this, board, target });
		script.setBinding(old);
		return result;
	}

	public Object callEvent(int event, VirtualCell target){
		switch (event){
			case Ability.SCRIPT_EVENT_CHECK:
				return callFunction(functionCheck, target);
			case Ability.SCRIPT_EVENT_CAST:
				return callFunction(functionCast, target);
		}
		return null;
	}

	public AbilityData getAbilityData(){
		return abilityData;
	}

	public Binding getScope() {
		return null;
	}

	public Script getScript(){
		return null;
	}

	public Unit getCaster() {
		return caster;
	}

	public void putOnCoolDown(){
		coolDownLeft = abilityData.getCoolDown();
	}

	public boolean onCoolDown(){
		return coolDownLeft > 0;
	}

	public Object callEvent(int event, Cell target) {
		return null;
	}

	public void updateCoolDown(){
		if (coolDownLeft > 0)
			coolDownLeft--;
	}

	public int getCoolDownLeft(){
		return coolDownLeft;
	}

	private void initScope(){
		scope = new Binding(new HashMap<Object, Object>(ability.getScope().getVariables()));

		script = ability.getScript();

		functionCheck = "onCheck";
		functionCast = "onCast";
	}

	public boolean equals(Object object){
		return (object instanceof VirtualAbility && ((VirtualAbility) object).ability == ability);
	}
}
