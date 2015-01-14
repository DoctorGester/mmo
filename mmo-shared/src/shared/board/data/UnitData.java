package shared.board.data;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import shared.other.DataLoaderKey;

import java.util.ArrayList;
import java.util.List;

public class UnitData {
	@DataLoaderKey
	private int id,
				health,
				damage,
				actionPoints,
				restTime;

	@DataLoaderKey private String name = "Undefined";
	@DataLoaderKey("script") private String scriptName;
	@DataLoaderKey("model") private String modelPath;
	@DataLoaderKey("scale") private double scale = 1.0;

	@DataLoaderKey(value = "spell", function = "addSpell")
	private List<String> spells = new ArrayList<String>();

	@DataLoaderKey(value = "passive", function = "addPassive")
	private List<String> passives = new ArrayList<String>();

	public void addSpell(String spell){
		spells.add(spell);
	}

	public void addPassive(String passive) {
		passives.add(passive);
	}

	public List<String> getSpells(){
		return spells;
	}

	public List<String> getPassives() {
		return passives;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRestTime() {
		return restTime;
	}

	public int getHealth() {
		return health;
	}

	public int getDamage() {
		return damage;
	}

	public int getActionPoints() {
		return actionPoints;
	}

	public double getScale() {
		return scale;
	}

	public String getModelPath() {
		return modelPath;
	}

	public Script compileScript(GroovyScriptEngine engine, Binding binding){
		try {
			return engine.createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
