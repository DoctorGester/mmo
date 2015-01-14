package core.main;

import shared.map.Faction;

import java.util.HashMap;
import java.util.Map;

public class ServerFaction implements Faction {
    private static int globalIdCounter = -1;

    private int id;
    private String name;

    private Map<Integer, Integer> attitude;

    public ServerFaction(String name){
        this.name = name;

        id = ++globalIdCounter;

        attitude = new HashMap<Integer, Integer>();
    }

    public ServerFaction setAttitudeTo(Faction faction, int attitude){
        this.attitude.put(faction.getId(), attitude);
        return this;
    }

    public ServerFaction setAttitudeBoth(ServerFaction faction, int attitude){
        setAttitudeTo(faction, attitude);
        faction.setAttitudeTo(this, attitude);
        return this;
    }

    public int getAttitudeTo(Faction faction){
        return attitude.get(faction.getId());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
}
