package core.main;

/**
 * @author doc
 */
public enum Stat {
	WORSHIP_VI("Vi"),
	WORSHIP_YUI("Yui"),
	WORSHIP_SETHI("Sethi");

	private String name;

	Stat(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}
}
