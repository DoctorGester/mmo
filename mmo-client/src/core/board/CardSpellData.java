package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoaderKey;

public class CardSpellData {
	@DataLoaderKey private String id;
	@DataLoaderKey private String script;

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

	public Script compileScript(Binding binding){
		try {
			return Program.getInstance().getScriptEngine().createScript(script, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
