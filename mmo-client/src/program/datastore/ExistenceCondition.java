package program.datastore;

/**
 * @author doc
 */
public class ExistenceCondition implements Condition {
	private String key;

	public ExistenceCondition(String key) {
		this.key = key;
	}

	@Override
	public boolean check() {
		return DataStore.getInstance().get(key) != null;
	}
}
