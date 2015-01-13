package program.datastore;

/**
 * @author doc
 */
public class Data {
	private long date = System.currentTimeMillis();
	private Object object;

	public Data(Object object) {
		this.object = object;
	}

	public long getDate() {
		return date;
	}

	public Object getObject() {
		return object;
	}

	public <T> T getObject(Class<T> type){
		if (type.isInstance(object))
			return type.cast(object);

		return null;
	}

	public boolean equals(Object o){
		return o instanceof Data && object.equals(o);
	}

	public int hashCode() {
		return object.hashCode();
	}
}
