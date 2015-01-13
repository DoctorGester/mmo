package core.board;

import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoader;
import program.main.data.DataLoaderKey;

import java.io.IOException;
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

	private Spatial model;

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

	public Script compileScript(Binding binding){
		try {
			return Program.getInstance().getScriptEngine().createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Spatial getModel(boolean copy) {
		if (model == null)
			loadModel(Program.getInstance().getDataLoader());
		if (copy)
			return model.clone(false);
		return model;
	}

	public void setModel(Spatial model) {
		this.model = model;
	}

	public void loadModel(DataLoader loader){
		try {
			model = loader.loadAnimatedModelAlt(modelPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		model.setLocalScale((float) scale);
		model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
	}
}
