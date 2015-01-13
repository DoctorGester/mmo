package program.datastore;

/**
 * @author doc
 */
public class ActualCondition extends DateCondition {
	public ActualCondition(String key) {
		super(key, System.currentTimeMillis());
	}
}
