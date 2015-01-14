package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import shared.board.Board;
import shared.board.Cell;
import shared.board.Spell;
import shared.board.Unit;
import shared.board.data.SpellData;

public class ClientSpell implements Spell {
	private SpellData spellData;
	private ClientUnit caster;
	private Board board;

	private Script script;
	private Binding scope;

	private String functionCheck, functionCastBegin, functionCastEnd, functionCheckAOE;

	private int coolDownLeft;

	public ClientSpell(SpellData spellData, ClientUnit caster, Board board) {
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

	public ClientUnit getCaster(){
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
		functionCastBegin = "onCastBegin";
		functionCastEnd = "onCastEnd";
		functionCheckAOE = "onCheckAOE";
	}
}
