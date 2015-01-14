package core.board.ai;

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import program.main.Program;
import shared.board.Buff;
import shared.board.data.BuffData;

public class VirtualBuff implements Buff {
	private VirtualBoard board;
	private BuffData buffData;
	private int timesToRepeat, period;
	private Object data;

	private int time;
	private boolean endless;
	private boolean doNotTick;
	private boolean onInitCalled, hasEnded;

	private GroovyObject script;
	private Binding scope;

	private String functionInit, functionTick, functionEnd;

	public VirtualBuff(VirtualBoard board, BuffData buffData, int timesToRepeat, int period, int initialDelay, Object data) {
		this.board = board;
		this.buffData = buffData;
		this.timesToRepeat = timesToRepeat;
		this.period = period;
		this.data = data;

		this.time = initialDelay;

		this.endless = timesToRepeat == 0;
		this.doNotTick = period == Integer.MAX_VALUE;

		initScope();
	}

	public VirtualBuff(VirtualBoard board, BuffData buffData, int timesToRepeat, int period, Object data){
		this(board, buffData, timesToRepeat, period, period, data);
	}

	public VirtualBuff(VirtualBoard board, BuffData buffData, int timesToRepeat, int period){
		this(board, buffData, timesToRepeat, period, null);
	}

	private void initScope(){
		scope = new Binding();

		script = buffData.compileScript(Program.getInstance().getScriptEngine(), scope);

		functionInit = "onInit";
		functionTick = "onTick";
		functionEnd = "onEnd";
	}

	private void callFunction(String function){
		if (script.getMetaClass().respondsTo(script, function).isEmpty())
			return;
		script.invokeMethod(function, new Object[] { this, board });
	}

	public boolean hasEnded() {
		return hasEnded;
	}

	public Object getData() {
		return data;
	}

	public void end(){
		callFunction(functionEnd);
		hasEnded = true;
	}

	public void update(){
		if (hasEnded)
			return;
		if (!onInitCalled){
			callFunction(functionInit);
			onInitCalled = true;
		}
		if (!doNotTick) {
			time--;
			if (time <= 0) {
				callFunction(functionTick);
				time = period;

				if (!endless) {
					timesToRepeat--;
					if (timesToRepeat <= 0)
						end();
				}
			}
		}
	}
}
