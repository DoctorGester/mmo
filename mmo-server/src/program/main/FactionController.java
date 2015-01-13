package program.main;

import core.main.Faction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class FactionController {
	protected Map<Integer, Faction> idFactionMap = new HashMap<Integer, Faction>();

	public Collection<Faction> getFactions(){
		return idFactionMap.values();
	}

	public Faction getFactionById(int id){
		return idFactionMap.get(id);
	}

	private void addFaction(Faction faction){
		idFactionMap.put(faction.getId(), faction);
	}

	public void createFactions(){
		Faction mongoloids = new Faction("Mongoloids"),
				rusians = new Faction("Rusians"),
				beetards = new Faction("Bee Tards"),
				weaboos = new Faction("Weaboos"),
				fagets = new Faction("Fagets"),
				neutrals = new Faction("Neutrals");
		addFaction(mongoloids);
		addFaction(rusians);
		addFaction(beetards);
		addFaction(weaboos);
		addFaction(fagets);
		addFaction(neutrals);

		mongoloids.setAttitudeBoth(rusians, 30)
				.setAttitudeBoth(beetards, 20)
				.setAttitudeBoth(weaboos, 60)
				.setAttitudeBoth(fagets, -10)
				.setAttitudeBoth(neutrals, -100);

		rusians.setAttitudeBoth(beetards, 20)
				.setAttitudeBoth(weaboos, -10)
				.setAttitudeBoth(fagets, 60)
				.setAttitudeBoth(neutrals, -100);

		beetards.setAttitudeBoth(weaboos, 50)
				.setAttitudeBoth(fagets, -10)
				.setAttitudeBoth(neutrals, 100);

		weaboos.setAttitudeBoth(fagets, 0)
				.setAttitudeBoth(neutrals, -100);

		fagets.setAttitudeBoth(neutrals, 0);
	}

}
