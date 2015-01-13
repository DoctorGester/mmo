package core.board;

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import program.main.Program;
import program.main.data.DataLoaderKey;

public class BuffData {
	@DataLoaderKey
	private String id;

	@DataLoaderKey
	private String script;

	private Class<?> compiledClass;

	public String getId() {
		return id;
	}

	public GroovyObject compileScript(Binding binding){
		try {
			return Program.getInstance().getScriptEngine().createScript(script, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
