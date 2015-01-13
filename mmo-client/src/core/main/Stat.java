package core.main;

/**
 * @author doc
 */
public enum Stat {
	WORSHIP_VI("Vi worship"),
	WORSHIP_YUI("Yui worship"),
	WORSHIP_SETHI("Sethi worship");

	private String name;

	Stat(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}
}
