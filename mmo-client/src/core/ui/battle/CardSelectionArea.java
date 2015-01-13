package core.ui.battle;

import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import core.ui.ItemElement;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.core.utils.UIDUtil;
import tonegod.gui.listeners.KeyboardListener;
import tonegod.gui.listeners.MouseMovementListener;
import tonegod.gui.listeners.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class CardSelectionArea<T extends ItemElement> extends Element implements Control,
														  MouseMovementListener,
														  MouseWheelListener,
														  KeyboardListener {



	private static final float CARD_MOVING_SPEED_PERCENT = 0.02f;
	private static final int MAX_CARDS_AT_ONE_SIDE = 5;

	private List<T> cards = new ArrayList<T>();
	private int currentSelected;

	public CardSelectionArea(Screen screen, Vector2f position, Vector2f size) {
		super(screen, UIDUtil.getUID(), position, size, new Vector4f(), null);

		setInitialized();
		addControl(this);
	}

	public List<T> getCards() {
		return cards;
	}

	public void addCard(T cardElement){
		cards.add(cardElement);
		addChild(cardElement);
		cardElement.setMouseWheelDispatcher(this);
		cardElement.setZOrder(cardElement.getZOrder() + 1f);
	}

	public void removeCard(T cardElement){
		cards.remove(cardElement);

		if (cards.size() >= currentSelected)
			currentSelected = cards.size() - 1;

		removeChild(cardElement);
	}

	public void clear(){
		removeAllChildren();
		cards.clear();
		currentSelected = 0;
	}

	public void onMouseWheelUp(MouseMotionEvent evt) {
		if (currentSelected < cards.size() - 1)
			currentSelected++;
		evt.setConsumed();
	}

	public void onMouseWheelDown(MouseMotionEvent evt) {
		if (currentSelected > 0)
			currentSelected--;
		evt.setConsumed();
	}

	public void onKeyPress(KeyInputEvent keyInputEvent) {
	}

	public void onKeyRelease(KeyInputEvent keyInputEvent) {
	}

	public void onMouseLeftPressed(MouseButtonEvent mouseButtonEvent) {
	}

	public void onMouseLeftReleased(MouseButtonEvent mouseButtonEvent) {
	}

	public void onMouseRightPressed(MouseButtonEvent mouseButtonEvent) {
	}

	public void onMouseRightReleased(MouseButtonEvent mouseButtonEvent) {
	}

	public void onMouseMove(MouseMotionEvent mouseMotionEvent) {
	}

	public void update(float tpf) {
		if (cards.size() == 0)
			return;

		float cardMovingSpeed = screen.getWidth() * CARD_MOVING_SPEED_PERCENT;

		Vector2f size = getDimensions();

		ItemElement currentCard = cards.get(currentSelected);
		Vector2f absoluteCenter = new Vector2f(size.x / 2f, size.y / 2f);

		for(int i = 0, range = currentSelected; i < cards.size(); i++, range = currentSelected - i){
			ItemElement card = cards.get(i);

			if (Math.abs(range) > MAX_CARDS_AT_ONE_SIDE){
				card.setIsVisible(false);
				continue;
			}

			card.setIsVisible(true);

			if (card.isBeingDragged())
				continue;

			if (card.isBeingFlipped()){
				card.updateFlip();
				continue;
			}

			float cardWidth = currentCard.getWidth();
			Vector2f toPosition = absoluteCenter.subtract(cardWidth / 2f - range * cardWidth * 1.2f, currentCard.getHeight() / 2f);
			Vector2f currentPosition = card.getPosition();

			if (toPosition.subtract(currentPosition).length() < cardMovingSpeed){
				card.setPosition(toPosition);
				continue;
			}

			Vector2f to = toPosition.subtract(currentPosition).normalizeLocal().multLocal(cardMovingSpeed);

			card.setPosition(currentPosition.add(to));
		}
	}

	public void setSpatial(Spatial spatial) {}
	public void render(RenderManager rm, ViewPort vp) {}
	public void onMouseWheelPressed(MouseButtonEvent evt) {}
	public void onMouseWheelReleased(MouseButtonEvent evt) {}

	public Control cloneForSpatial(Spatial spatial) {
		return null;
	}
}
