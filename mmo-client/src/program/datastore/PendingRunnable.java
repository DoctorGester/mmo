package program.datastore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author doc
 */
public class PendingRunnable {
	private Set<Condition> requirements = new HashSet<Condition>();
	private long timeInitiated = System.currentTimeMillis();
	private Runnable runnable;
	private boolean onDestroy;

	protected PendingRunnable(Runnable runnable, Condition... conditions){
		this.requirements.addAll(Arrays.asList(conditions));
		this.runnable = runnable;
	}

	public Set<Condition> getRequirements() {
		return requirements;
	}

	public void execute(){
		runnable.run();
		onDestroy = true;
	}

	public boolean isOnDestroy() {
		return onDestroy;
	}
}
