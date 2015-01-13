package core.main;

import java.util.HashMap;
import java.util.Map;

public class Faction {
    private static int globalIdCounter = -1;

    private int id;
    private String name;

    private Map<Integer, Integer> attitude;

    public Faction(String name){
        this.name = name;

        id = ++globalIdCounter;

        attitude = new HashMap<Integer, Integer>();
    }

    public Faction setAttitudeTo(Faction faction, int attitude){
        this.attitude.put(faction.getId(), attitude);
        return this;
    }

    public Faction setAttitudeBoth(Faction faction, int attitude){
        setAttitudeTo(faction, attitude);
        faction.setAttitudeTo(this, attitude);
        return this;
    }

    public int getAttitudeTo(Faction faction){
        return attitude.get(faction.getId());
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }
}
