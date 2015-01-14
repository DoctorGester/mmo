package shared.board.data;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import shared.other.DataLoaderKey;

public class PassiveData {
	@DataLoaderKey private String id;
	@DataLoaderKey("script") private String scriptName;

	// Client unique data
	@DataLoaderKey private String name = "Undefined";
	@DataLoaderKey private String description = "Tool tip missing!";
	@DataLoaderKey private String icon;

	public String getId() {
		return id;
	}

	public String getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Script compileScript(GroovyScriptEngine engine, Binding binding) {
		try {
			return engine.createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
