package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoaderKey;

import java.util.HashSet;
import java.util.Set;

public class SpellData {
	@DataLoaderKey private String id;
	@DataLoaderKey private int coolDown;

	@DataLoaderKey(value = "target", function = "addTarget", dataEnum = SpellTarget.class)
	private Set<SpellTarget> targets = new HashSet<SpellTarget>();

	@DataLoaderKey("script")
	private String scriptName;

	public void addTarget(SpellTarget target){
		targets.add(target);
	}

	public boolean targetAllowed(SpellTarget target){
		return targets.contains(target);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCoolDown() {
		return coolDown;
	}

	public Script compileScript(Binding binding){
		try {
			return Program.getInstance().getScriptEngine().createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
