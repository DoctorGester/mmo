package shared.board.data;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import shared.other.DataElement;
import shared.other.DataLoaderKey;

public class SpellData extends DataElement {
	@DataLoaderKey private String script;

	// Client unique data
	@DataLoaderKey private String name = "Undefined";
	@DataLoaderKey private String description = "Tool tip missing!";
	@DataLoaderKey private String icon;

	public String getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Script compileScript(GroovyScriptEngine engine, Binding binding){
		try {
			return engine.createScript(script, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
