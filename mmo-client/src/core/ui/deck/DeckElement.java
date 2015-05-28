package core.ui.deck;

import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/**
* @author doc
*/
public class DeckElement {
	private Geometry geometry;
	private float progressCurrent;
	private float progressTarget;

	public DeckElement(Geometry geometry) {
		this.geometry = geometry;
	}

	public Spatial getModel() {
		return geometry;
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
}
