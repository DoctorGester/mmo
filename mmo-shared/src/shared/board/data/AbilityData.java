package shared.board.data;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import shared.board.AbilityTarget;
import shared.other.DataElement;
import shared.other.DataLoaderKey;

import java.util.HashSet;
import java.util.Set;

public class AbilityData implements DataElement {
	@DataLoaderKey(value = "target", function = "addTarget", dataEnum = AbilityTarget.class)
	private Set<AbilityTarget> targets = new HashSet<AbilityTarget>();

	@DataLoaderKey private String id;
	@DataLoaderKey private int coolDown;
	@DataLoaderKey("script") private String scriptName;

	// Client unique data
	@DataLoaderKey private String name = "Undefined";
	@DataLoaderKey private String description = "Tool tip missing!";
	@DataLoaderKey private String icon;

	public boolean targetAllowed(AbilityTarget target) {
		return targets.contains(target);
	}

	public boolean onlyAllowed(AbilityTarget target) {
		return targets.size() == 1 && targets.contains(target);
	}

	public void addTarget(AbilityTarget target) {
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

	public Script compileScript(GroovyScriptEngine engine, Binding binding) {
		try {
			return engine.createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
