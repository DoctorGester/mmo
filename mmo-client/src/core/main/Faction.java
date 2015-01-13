package core.main;

public class Faction {
    public static final String UNDEFINED_NAME = "Undefined";

    private int id;
    private String name = UNDEFINED_NAME;

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }
}
