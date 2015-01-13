package core.board;

import core.board.interfaces.Board;
import core.board.interfaces.Passive;
import core.board.interfaces.Unit;
import groovy.lang.Binding;
import groovy.lang.GroovyObject;

public class PassiveImpl implements Passive {
	private Board board;
	private PassiveData passiveData;
	private Unit owner;

	private boolean onInitCalled;

	private GroovyObject script;
	private Binding scope;

	private String functionInit, functionTick;

	public PassiveImpl(PassiveData passiveData, Unit owner, Board board) {
		this.passiveData = passiveData;
		this.owner = owner;
		this.board = board;

		initScope();
	}

	private void initScope(){
		scope = new Binding();

		script = passiveData.compileScript(scope);

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
