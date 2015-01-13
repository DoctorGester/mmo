package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoaderKey;

import java.util.HashSet;
import java.util.Set;

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

	public Script compileScript(Binding binding) {
		try {
			return Program.getInstance().getScriptEngine().createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
