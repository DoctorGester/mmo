package core.board;

import core.board.interfaces.Board;
import core.main.CardMaster;
import groovy.lang.Binding;
import groovy.lang.Script;

public class CardSpell {
	private CardSpellData spellData;
	private CardMaster caster;
	private Board board;

	private Script script;
	private Binding scope;

	private static final String function = "onCast";

	public CardSpell(CardSpellData spellData, CardMaster caster, Board board) {
		this.spellData = spellData;
		this.caster = caster;
		this.board = board;

		initScope();
	}

	public void invokeScript(){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return;
		script.invokeMethod(function, new Object[] { this, board, caster });
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

		script = spellData.compileScript(scope);
	}
}
