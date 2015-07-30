package core.ui.deck;

import com.jme3.scene.Spatial;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;

/**
 * @author doc
 */
public class CardHoverListener implements CursorListener {
	private DeckElement deckElement;
	private boolean isHovered = false;

	public CardHoverListener(DeckElement deckElement) {
		this.deckElement = deckElement;
	}

	@Override
	public void cursorButtonEvent(CursorButtonEvent cursorButtonEvent, Spatial target, Spatial capture) {
	}

	@Override
	public void cursorEntered(CursorMotionEvent cursorMotionEvent, Spatial target, Spatial capture) {
		cursorMotionEvent.setConsumed();

		isHovered = true;
	}

	@Override
	public void cursorExited(CursorMotionEvent cursorMotionEvent, Spatial target, Spatial capture) {
		cursorMotionEvent.setConsumed();

		isHovered = false;
	}

	@Override
	public void cursorMoved(CursorMotionEvent cursorMotionEvent, Spatial target, Spatial capture) {

	}

	public boolean isHovered() {
		return isHovered;
	}
}
