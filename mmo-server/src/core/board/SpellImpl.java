package core.board;

import core.board.interfaces.Board;
import core.board.interfaces.Cell;
import core.board.interfaces.Spell;
import core.board.interfaces.Unit;
import groovy.lang.Binding;
import groovy.lang.Script;

public class SpellImpl implements Spell {
	private SpellData spellData;
	private Unit caster;
	private Board board;

	private Script script;
	private Binding scope;

	private String functionCheck, functionCast;

	private int coolDownLeft;

	public SpellImpl(SpellData spellData, Unit caster, Board board) {
		this.spellData = spellData;
		this.caster = caster;
		this.board = board;

		initScope();
	}

	private Object callFunction(String function, Cell target){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return null;
		return script.invokeMethod(function, new Object[] { this, board, target });
	}

	public Object callEvent(int event, Cell target){
		switch (event){
			case SCRIPT_EVENT_CHECK:
				return callFunction(functionCheck, target);
			case SCRIPT_EVENT_CAST:
				return callFunction(functionCast, target);
		}
		return null;
	}

	public Unit getCaster(){
		return caster;
	}

	public SpellData getSpellData(){
		return spellData;
	}

	public void putOnCoolDown(){
		coolDownLeft = spellData.getCoolDown();
	}

	public boolean onCoolDown(){
		return coolDownLeft > 0;
	}

	public void updateCoolDown(){
		if (coolDownLeft > 0)
			coolDownLeft--;
	}

	public int getCoolDownLeft(){
		return coolDownLeft;
	}

	public Binding getScope() {
		return scope;
	}

	public Script getScript() {
		return script;
	}

	private void initScope(){
		scope = new Binding();

		script = spellData.compileScript(scope);

		functionCheck = "onCheck";
		functionCast = "onCast";
	}
}
