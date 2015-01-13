package gui.core;

import com.jme3.math.Vector2f;

public interface UIMouseListener {
	public void leftButtonPressed(Vector2f point);

	public void leftButtonReleased(Element target, Vector2f point);

	public void mouseMoved(Vector2f point);

	public Element getElement();
}
