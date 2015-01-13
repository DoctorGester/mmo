package core.ui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.Screen;
import tonegod.gui.core.utils.UIDUtil;
import tonegod.gui.effects.Effect;
import tonegod.gui.listeners.MouseButtonListener;
import tonegod.gui.listeners.MouseWheelListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class ItemElement extends Element implements MouseWheelListener, MouseButtonListener{
	public static final float WIDTH_TO_HEIGHT = 0.67f;
	public static final float FLIP_SPEED = 0.5f;

	private MouseWheelListener mouseWheelDispatcher;
	private MouseButtonListener mouseButtonDispatcher;

	private boolean front = true;
	private boolean enabled = true;

	private Effect slideEffect = null;

	private boolean beingDragged, beingFlipped;
	private float flipSize;
	private int flipDir;

	private List<Element> frontElements = new ArrayList<Element>(),
						  backElements = new ArrayList<Element>();

	public ItemElement(ElementManager screen, Vector2f position, float height) {
		super(screen, UIDUtil.getUID(), position, new Vector2f(height * WIDTH_TO_HEIGHT, height), new Vector4f(), "res/textures/card.png");

		setIsMovable(true);
		setIsDragDropDragElement(true);

		showFront();
	}

	public void addFrontElement(Element element){
		frontElements.add(element);
		if (front)
			showFront();
	}

	public void addBackElements(Element element){
		backElements.add(element);
		if (!front)
			showBack();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (!enabled == this.enabled){
			this.enabled = enabled;
			ColorRGBA shade = new ColorRGBA();
			if (!enabled)
				shade = new ColorRGBA(0.5f, 0.5f, 0.5f, 1f);
			getElementMaterial().setColor("Color", shade);
			setIsMovable(enabled);
		}
	}

	public Effect getSlideEffect() {
		return slideEffect;
	}

	public void setSlideEffect(Effect slideEffect) {
		this.slideEffect = slideEffect;
	}

	private void showFront(){
		front = true;
		removeAllChildren();
		for (Element element: frontElements)
			addChild(element);
	}

	private void showBack(){
		front = false;
		removeAllChildren();
		for (Element element: backElements)
			addChild(element);
	}

	public boolean isBeingFlipped() {
		return beingFlipped;
	}

	public void flip(){
		beingFlipped = true;
		flipDir = -1;
		flipSize = 1;
	}

	public void updateFlip(){
		setPosition(getX() + getWidth() * (FLIP_SPEED / 2f) * -flipDir, getY());

		flipSize = Math.max(0, Math.min(flipSize + FLIP_SPEED * flipDir, 1));
		if (flipSize == 0 && flipDir == -1){
			flipDir = 1;
			if (front)
				showBack();
			else
				showFront();
		}
		if (flipSize == 1 && flipDir == 1)
			beingFlipped = false;

		setLocalScale(flipSize, 1, 1);
	}

	public void setMouseWheelDispatcher(MouseWheelListener mouseWheelDispatcher) {
		this.mouseWheelDispatcher = mouseWheelDispatcher;
	}

	public void setMouseButtonDispatcher(MouseButtonListener mouseButtonDispatcher) {
		this.mouseButtonDispatcher = mouseButtonDispatcher;
	}

	public void onMouseWheelPressed(MouseButtonEvent evt) {
		if (mouseWheelDispatcher != null)
			mouseWheelDispatcher.onMouseWheelPressed(evt);
	}

	public void onMouseWheelReleased(MouseButtonEvent evt) {
		if (mouseWheelDispatcher != null)
			mouseWheelDispatcher.onMouseWheelReleased(evt);
	}

	public void onMouseWheelUp(MouseMotionEvent evt) {
		if (mouseWheelDispatcher != null)
			mouseWheelDispatcher.onMouseWheelUp(evt);
	}

	public void onMouseWheelDown(MouseMotionEvent evt) {
		if (mouseWheelDispatcher != null)
			mouseWheelDispatcher.onMouseWheelDown(evt);
	}

	public void onMouseLeftPressed(MouseButtonEvent mouseButtonEvent) {
		beingDragged = true;
		setZOrder(getZOrder() + 1f);
		if (mouseButtonDispatcher != null)
			mouseButtonDispatcher.onMouseLeftPressed(mouseButtonEvent);
	}

	public void onMouseLeftReleased(MouseButtonEvent mouseButtonEvent) {
		beingDragged = false;
		setZOrder(getZOrder() - 1f);
		if (mouseButtonDispatcher != null)
			mouseButtonDispatcher.onMouseLeftReleased(mouseButtonEvent);
	}

	public void onMouseRightPressed(MouseButtonEvent mouseButtonEvent) {
		if (mouseButtonDispatcher != null)
			mouseButtonDispatcher.onMouseRightPressed(mouseButtonEvent);
		if (!beingFlipped && !beingDragged)
			flip();
	}

	public void onMouseRightReleased(MouseButtonEvent mouseButtonEvent) {
		if (mouseButtonDispatcher != null)
			mouseButtonDispatcher.onMouseRightReleased(mouseButtonEvent);
	}

	public boolean isBeingDragged() {
		return beingDragged;
	}
}
