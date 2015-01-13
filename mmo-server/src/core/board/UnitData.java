package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoaderKey;

import java.util.ArrayList;
import java.util.List;

public class UnitData {
	@DataLoaderKey
	private int id,
				health,
				damage,
				actionPoints,
				restTime;

	@DataLoaderKey("script")
	private String scriptName;

	@DataLoaderKey(value = "spell", function = "addSpell")
	private List<String> spells = new ArrayList<String>();

	@DataLoaderKey(value = "passive", function = "addPassive")
	private List<String> passives = new ArrayList<String>();

	public void addSpell(String spell){
		spells.add(spell);
	}

	public void addPassive(String spell){
		passives.add(spell);
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

	public Script compileScript(Binding binding){
		try {
			return Program.getInstance().getScriptEngine().createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
