package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoaderKey;

import java.util.HashSet;
import java.util.Set;

public class SpellData {
	@DataLoaderKey(value = "target", function = "addTarget", dataEnum = SpellTarget.class)
	private Set<SpellTarget> targets = new HashSet<SpellTarget>();

	@DataLoaderKey private String id;
	@DataLoaderKey private int coolDown;
	@DataLoaderKey("script") private String scriptName;

	// Client unique data
	@DataLoaderKey private String name = "Undefined";
	@DataLoaderKey private String description = "Tool tip missing!";
	@DataLoaderKey private String icon;

	public boolean targetAllowed(SpellTarget target) {
		return targets.contains(target);
	}

	public boolean onlyAllowed(SpellTarget target) {
		return targets.size() == 1 && targets.contains(target);
	}

	public void addTarget(SpellTarget target) {
		targets.add(target);
	}

	public String getId() {
		return id;
	}

	public int getCoolDown() {
		return coolDown;
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
