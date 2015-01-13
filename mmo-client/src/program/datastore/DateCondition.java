package program.datastore;

/**
 * @author doc
 */
public class DateCondition implements Condition {
	private String key;
	private long date;

	public DateCondition(String key, long date) {
		this.key = key;
		this.date = date;
	}

	@Override
	public boolean check() {
		return DataStore.getInstance().getData(key) != null &&
			   DataStore.getInstance().getData(key).getDate() >= date;
	}
}
