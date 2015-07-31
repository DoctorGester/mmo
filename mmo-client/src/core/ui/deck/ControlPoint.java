package core.ui.deck;

import com.jme3.math.Vector3f;

/**
* @author doc
*/
public class ControlPoint {
	private Vector3f position;
	private float rotation;

	public ControlPoint(Vector3f position, float rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public Vector3f getPosition() {
		return position.clone();
	}

	public float getRotation() {
		return rotation;
	}
}
