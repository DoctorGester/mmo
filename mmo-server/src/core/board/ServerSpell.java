package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import shared.board.Board;
import shared.board.Spell;
import shared.board.data.SpellData;
import shared.map.CardMaster;

public class ServerSpell implements Spell {
	private SpellData spellData;
	private CardMaster caster;
	private Board board;

	private Script script;
	private Binding scope;

	private static final String function = "onCast";

	public ServerSpell(SpellData spellData, CardMaster caster, Board board) {
		this.spellData = spellData;
		this.caster = caster;
		this.board = board;

		initScope();
	}

	private void invokeScript(){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return;
		script.invokeMethod(function, new Object[] { this, board, caster });
	}

	@Override
	public Object callEvent(int event) {
		if (event == SCRIPT_EVENT_CAST_BEGIN)
			invokeScript();

		return null;
	}

	public SpellData getSpellData(){
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
	}
}
