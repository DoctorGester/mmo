package shared.board.data;

import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;
import shared.other.DataLoaderKey;

public class BuffData {
	@DataLoaderKey private String id;
	@DataLoaderKey private String script;
	@DataLoaderKey private String icon;
	@DataLoaderKey private String name = "Undefined";
	@DataLoaderKey private String description = "Tool tip missing!";

	private Class<?> compiledClass;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	public GroovyObject compileScript(GroovyScriptEngine engine, Binding binding){
		try {
			return engine.createScript(script, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
