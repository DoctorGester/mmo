package program.datastore;

public interface Subscriber {
	public void receive(String key, Data subscription);
}
