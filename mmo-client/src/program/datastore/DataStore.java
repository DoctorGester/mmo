package program.datastore;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author doc
 */
public class DataStore {
	private static DataStore instance = new DataStore();

	private Map<String, Data> store = new HashMap<String, Data>();
	private final List<PendingRunnable> pending = new LinkedList<PendingRunnable>();

	private Map<String, List<Subscriber>> subscriberMap = new HashMap<String, List<Subscriber>>();

	public static DataStore getInstance(){
		return instance;
	}

	private DataStore(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new PendingProcessingTask(), 40, 40, TimeUnit.MILLISECONDS);
	}

	public void subscribe(String key, Subscriber subscriber){
		if (store.containsKey(key))
			subscriber.receive(key, store.get(key));

		List<Subscriber> subscribers = subscriberMap.get(key);

		if (subscribers == null){
			subscribers = new ArrayList<Subscriber>();
			subscriberMap.put(key, subscribers);
		}

		subscribers.add(subscriber);
	}

	public void put(String key, Object value){
		Data data = new Data(value);
		store.put(key, data);

		List<Subscriber> subscribers = subscriberMap.get(key);

		if (subscribers != null)
			for (Subscriber subscriber: subscribers)
				subscriber.receive(key, data);
	}

	public <T> T get(String key, Class<T> type){
		Data data = store.get(key);

		if (type.isInstance(data.getObject()))
			return type.cast(data.getObject());

		return null;
	}

	public Object get(String key){
		Data data = store.get(key);
		if (data == null)
			return null;
		return data.getObject();
	}

	public Data getData(String key){
		return store.get(key);
	}

	public void awaitAndExecute(Runnable runnable, Condition ... requirements){
		synchronized (pending){
			pending.add(new PendingRunnable(runnable, requirements));
		}
	}

	private class PendingProcessingTask implements Runnable{

		@Override
		public void run() {
			try {
				synchronized (pending){
					for (PendingRunnable runnable: new LinkedList<PendingRunnable>(pending)) {
						boolean canExecute = true;
						Set<Condition> requirements = runnable.getRequirements();
						for (Condition requirement: requirements) {
							if (!requirement.check()) {
								canExecute = false;
								break;
							}
						}

						if (canExecute)
							runnable.execute();
					}

					for(Iterator<PendingRunnable> iterator = pending.iterator(); iterator.hasNext(); ){
						PendingRunnable runnable = iterator.next();

						if (runnable.isOnDestroy())
							iterator.remove();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
