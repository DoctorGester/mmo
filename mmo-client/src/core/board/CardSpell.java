package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import shared.board.Board;
import shared.board.Cell;
import shared.board.data.CardSpellData;
import shared.map.CardMaster;

public class CardSpell {
	public static final int SCRIPT_EVENT_CAST_BEGIN = 0x00,
							SCRIPT_EVENT_CAST_END = 0x01;

	private CardSpellData spellData;
	private CardMaster caster;
	private Board board;

	private Script script;
	private Binding scope;

	private String functionCheck, functionCastBegin, functionCastEnd;

	public CardSpell(CardSpellData spellData, CardMaster caster, Board board) {
		this.spellData = spellData;
		this.caster = caster;
		this.board = board;

		initScope();
	}

	private Object callFunction(String function, Object ... args){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return null;
		return script.invokeMethod(function, args);
	}

	public boolean checkCell(Cell cell){
		return (Boolean) callFunction(functionCheck, this, board, caster, cell);
	}

	public Object callEvent(int event){
		switch (event){
			case SCRIPT_EVENT_CAST_BEGIN:
				return callFunction(functionCastBegin, this, board, caster);
			case SCRIPT_EVENT_CAST_END:
				return callFunction(functionCastEnd, this, board, caster);
		}
		return null;
	}

	public CardSpellData getSpellData(){
		return spellData;
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
	}
}
