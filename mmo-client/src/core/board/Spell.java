package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;

public class Spell {
	public static final int SCRIPT_EVENT_CHECK = 0x00,
							SCRIPT_EVENT_CAST_BEGIN = 0x01,
							SCRIPT_EVENT_CAST_END = 0x02;

	private SpellData spellData;
	private Unit caster;
	private Board board;

	private Script script;
	private Binding scope;

	private String functionCheck, functionCastBegin, functionCastEnd, functionCheckAOE;

	private int coolDownLeft;

	public Spell(SpellData spellData, Unit caster, Board board) {
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
			case SCRIPT_EVENT_CAST_BEGIN:
				return callFunction(functionCastBegin, target);
			case SCRIPT_EVENT_CAST_END:
				return callFunction(functionCastEnd, target);
		}
		return null;
	}

	public boolean checkAOE(Cell from, Cell to){
		if (script.getMetaClass().respondsTo(script, functionCheckAOE, new Object[] { this, board, from, to }).isEmpty())
			return true;
		return (Boolean) script.invokeMethod(functionCheckAOE, new Object[] { this, board, from, to });
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
		functionCastBegin = "onCastBegin";
		functionCastEnd = "onCastEnd";
		functionCheckAOE = "onCheckAOE";
	}
}
