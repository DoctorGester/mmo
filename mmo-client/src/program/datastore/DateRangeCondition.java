package program.datastore;

/**
 * @author doc
 */
public class DateRangeCondition implements Condition {
	private String key;
	private long range;

	public DateRangeCondition(String key, long range) {
		this.key = key;
		this.range = range;
	}

	@Override
	public boolean check() {
		return DataStore.getInstance().getData(key) != null &&
			   DataStore.getInstance().getData(key).getDate() - range <= 0;
	}
}
