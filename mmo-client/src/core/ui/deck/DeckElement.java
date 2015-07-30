package core.ui.deck;

import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

/**
* @author doc
*/
public class DeckElement {
	private Spatial model;
	private float progressCurrent;
	private float progressTarget;
	private float floatingStep;
	private int floatDirection = 1;

	public DeckElement(Spatial model) {
		this.model = model;
		floatingStep = FastMath.nextRandomFloat() * 2f - 1f;
	}

	public Spatial getModel() {
		return model;
	}

	public float getProgressCurrent() {
		return progressCurrent;
	}

	public void setProgressCurrent(float progressCurrent) {
		this.progressCurrent = progressCurrent;
	}

	public float getProgressTarget() {
		return progressTarget;
	}

	public void setProgressTarget(float progressTarget) {
		this.progressTarget = progressTarget;
	}

	public float getFloatingStep() {
		return floatingStep;
	}

	/**
	 *
	 * @return sign Movement direction
	 */
	public float updateMovement(float speed){
		float sign = Math.signum(progressTarget - progressCurrent);
		float progress = progressCurrent + sign * speed;

		if (sign * (progressTarget - progressCurrent) < speed) {
			progress = progressTarget;
		}

		progressCurrent = progress;

		float abs = Math.abs(floatDirection);
		floatingStep = FastMath.clamp(floatingStep + floatDirection * speed * 2, -abs, abs);

		if (floatingStep == floatDirection) {
			floatDirection *= -1;
		}

		return sign;
	}
}
