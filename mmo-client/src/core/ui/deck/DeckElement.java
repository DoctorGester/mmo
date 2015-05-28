package core.ui.deck;

import com.jme3.scene.Spatial;

/**
* @author doc
*/
public class DeckElement {
	private Spatial model;
	private float progressCurrent;
	private float progressTarget;

	public DeckElement(Spatial model) {
		this.model = model;
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
}
