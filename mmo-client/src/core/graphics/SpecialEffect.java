package core.graphics;

import groovy.lang.Script;
import program.main.Program;

/**
 * @author doc
 */
public class SpecialEffect {
	private static final String INIT_METHOD_NAME = "eventInit",
						 		UPDATE_METHOD_NAME = "eventUpdate";

	private Script script;

	public SpecialEffect(String id, Object ... arguments){
		script = Program.getInstance().getEffectScriptById(id);
		call(INIT_METHOD_NAME, arguments);
	}

	private Object call(String function, Object ... arguments){
		if (!script.getMetaClass().respondsTo(script, function).isEmpty())
			return script.invokeMethod(function, arguments);
		return null;
	}

	/**
	 *
	 * @return true if effect is ready to be removed, otherwise returns false
	 */
	public boolean update(float tpf){
		Object result = call(UPDATE_METHOD_NAME, tpf);
		if (result instanceof Boolean)
			return (Boolean) result;
		return false;
	}
}
