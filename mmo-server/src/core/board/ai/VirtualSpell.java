package core.board.ai;

import core.board.*;
import core.board.interfaces.Cell;
import core.board.interfaces.Spell;
import core.board.interfaces.Unit;
import groovy.lang.Binding;
import groovy.lang.Script;

import java.util.HashMap;

public class VirtualSpell implements Spell {
	private SpellData spellData;
	private VirtualUnit caster;
	private VirtualBoard board;

	private Spell spell;
	private Script script;
	private Binding scope;

	private String functionCheck, functionCast;

	private int coolDownLeft;

	public VirtualSpell(Spell spell, VirtualUnit caster, VirtualBoard board) {
		this.spell = spell;
		this.spellData = spell.getSpellData();
		this.caster = caster;
		this.board = board;
		this.coolDownLeft = spell.getCoolDownLeft();

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
			case Spell.SCRIPT_EVENT_CHECK:
				return callFunction(functionCheck, target);
			case Spell.SCRIPT_EVENT_CAST:
				return callFunction(functionCast, target);
		}
		return null;
	}

	public SpellData getSpellData(){
		return spellData;
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
		coolDownLeft = spellData.getCoolDown();
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
		scope = new Binding(new HashMap<Object, Object>(spell.getScope().getVariables()));

		script = spell.getScript();

		functionCheck = "onCheck";
		functionCast = "onCast";
	}

	public boolean equals(Object object){
		return (object instanceof VirtualSpell && ((VirtualSpell) object).spell == spell);
	}
}
