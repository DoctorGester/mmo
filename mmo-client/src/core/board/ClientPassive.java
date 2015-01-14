package core.board;

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import program.main.Program;
import shared.board.Board;
import shared.board.Passive;
import shared.board.Unit;
import shared.board.data.PassiveData;

public class ClientPassive implements Passive {
	private Board board;
	private PassiveData passiveData;
	private Unit owner;

	private boolean onInitCalled;

	private GroovyObject script;
	private Binding scope;

	private String functionInit, functionTick;

	public ClientPassive(PassiveData passiveData, Unit owner, Board board) {
		this.passiveData = passiveData;
		this.owner = owner;
		this.board = board;

		initScope();
	}

	private void initScope(){
		scope = new Binding();

		script = passiveData.compileScript(Program.getInstance().getScriptEngine(), scope);

		functionInit = "onInit";
		functionTick = "onTick";
	}

	private void callFunction(String function){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return;

		script.invokeMethod(function, new Object[] { this, board, owner });
	}

	public void update(){
		if (!onInitCalled){
			callFunction(functionInit);
			onInitCalled = true;
		} else {
			callFunction(functionTick);
		}
	}
}
