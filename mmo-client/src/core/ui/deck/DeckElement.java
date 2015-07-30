package core.ui.deck;

import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorEventControl;

/**
* @author doc
*/
public class DeckElement {
	private Spatial model;
	private float progressCurrent;
	private float progressTarget;
	private float floatingStep;
	private float hoverStep;
	private int floatDirection = 1;

	private CardHoverListener hoverListener;

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

	public void registerHoverListener(){
		hoverListener = new CardHoverListener(this);

		CursorEventControl.addListenersToSpatial(model, hoverListener);
	}

	public void removeHoverListener(){
		CursorEventControl.removeListenersFromSpatial(model, hoverListener);

		hoverListener = null;
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

		if (hoverListener != null) {
			int direction = hoverListener.isHovered() ? 1 : -1;
			hoverStep = FastMath.clamp(hoverStep + direction * speed, -1, 1);
		}

		return sign;
	}
}
