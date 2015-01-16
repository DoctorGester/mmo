package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import shared.board.Board;
import shared.board.Cell;
import shared.board.Spell;
import shared.board.Unit;
import shared.board.data.SpellData;

public class ServerSpell implements Spell {
	private SpellData spellData;
	private Unit caster;
	private Board board;

	private Script script;
	private Binding scope;

	private String functionCheck, functionCast;

	private int coolDownLeft;

	public ServerSpell(SpellData spellData, Unit caster, Board board) {
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

		script = spellData.compileScript(Program.getInstance().getScriptEngine(), scope);

		functionCheck = "onCheck";
		functionCast = "onCast";
	}
}