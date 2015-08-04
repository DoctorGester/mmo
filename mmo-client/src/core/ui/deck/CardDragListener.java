package core.ui.deck;

import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;

/**
 * @author doc
 */
public class CardDragListener implements CursorListener {
	private boolean dragging;

	@Override
	public void cursorButtonEvent(CursorButtonEvent event, Spatial target, Spatial capture) {
		if (event.getButtonIndex() == 0) {
			dragging = event.isPressed();
		}
	}

	@Override
	public void cursorEntered(CursorMotionEvent cursorMotionEvent, Spatial target, Spatial capture) {

	}

	@Override
	public void cursorExited(CursorMotionEvent cursorMotionEvent, Spatial target, Spatial capture) {

	}

	@Override
	public void cursorMoved(CursorMotionEvent cursorMotionEvent, Spatial target, Spatial capture) {
		if (dragging) {
			capture.setLocalTranslation(capture.getLocalTranslation().add(0.1f, 0, 0));
		}
	}

	public boolean isDragging() {
		return dragging;
	}
}
