package core.ui.map;

import core.main.Faction;
import shared.map.Stat;

import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class ProfileInfo {
	private int id = -1;
	private String name = "Undefined";
	private Map<Stat, Integer> stats = new HashMap<Stat, Integer>();
	private Map<Faction, Integer> reputation = new HashMap<Faction, Integer>();

	public ProfileInfo(){
		for(Stat stat: Stat.values())
			setStat(stat, 0);
	}

	public void setStat(Stat stat, int value){
		stats.put(stat, value);
	}

	public int getStat(Stat stat){
		return stats.get(stat);
	}

	public void setReputation(Faction faction, int reputation){
		this.reputation.put(faction, reputation);
	}

	public int getReputation(Faction faction){
		if (!reputation.containsKey(faction))
			return 0;
		return reputation.get(faction);
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

	public static String getKey(int id){
		return "profileInfo." + id;
	}
}
