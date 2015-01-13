package gui.core;

import com.jme3.app.Application;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.font.BitmapFont;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Screen implements Control, RawInputListener {
	private final Node root = new Node("gui-root");
	private Application application;
	private String elementShader;
	private Texture atlas;
	private BitmapFont font;

	private List<UIMouseListener> listeners = new ArrayList<UIMouseListener>();

	private ElementContainer elementContainer;

	public Screen(Application application, String atlasPath, String elementShader, String fontPath) {
		this.application = application;
		this.elementShader = elementShader;

		atlas = application.getAssetManager().loadTexture(atlasPath);
		atlas.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		atlas.setMagFilter(Texture.MagFilter.Bilinear);
		atlas.setWrap(Texture.WrapMode.Clamp);

		font = application.getAssetManager().loadFont(fontPath);

		elementContainer = new ElementContainer(Vector2f.ZERO, V.f(application.getCamera().getWidth(), application.getCamera().getHeight()), Vector4f.ZERO, Vector4f.ZERO);
		elementContainer.initialize(this);

		root.attachChild(elementContainer);
		root.setLocalTranslation(0, application.getCamera().getHeight(), 0);
		root.rotate(FastMath.PI, 0f, 0f);

		application.getInputManager().addRawInputListener(this);
	}

	public ElementContainer getElementContainer(){
		return elementContainer;
	}

	public void addMouseListener(UIMouseListener listener){
		listeners.add(listener);
	}

	@Override
	public void setSpatial(Spatial spatial) {
		if (spatial != null)
			((Node) spatial).attachChild(root);
	}

	public Texture getAtlas() {
		return atlas;
	}

	public Material createElementMaterial(){
		Material material = new Material(application.getAssetManager(), elementShader);
		material.setColor("Color", new ColorRGBA(1, 1, 1, 1));
		material.setTexture("ColorMap", atlas);
		material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);

		return material;
	}

	public BitmapFont createFont(){
		BitmapFont font = new BitmapFont();
		font.setCharSet(this.font.getCharSet());

		Material[] pages = new Material[this.font.getPageSize()];
		for (int i = 0; i < pages.length; i++)
			pages[i] = this.font.getPage(i).clone();

		font.setPages(pages);

		return font;
	}

	private boolean leftButtonPressed(Vector2f point){
		for (UIMouseListener listener: listeners)
			if (listener.getElement().contains(point)){
				listener.leftButtonPressed(point);
				return true;
			}

		return false;
	}

	private boolean leftButtonReleased(Vector2f point){
		UIMouseListener target = null;

		for (UIMouseListener listener: listeners)
			if (listener.getElement().contains(point)){
				target = listener;
				break;
			}

		for (UIMouseListener listener: listeners)
			listener.leftButtonReleased(target == null ? null : target.getElement(), point);

		return target != null;
	}

	@Override
	public void update(float tpf) {
		elementContainer.update(tpf);
	}

	@Override
	public void render(RenderManager rm, ViewPort vp) {}

	@Override
	public void onMouseMotionEvent(MouseMotionEvent event) {
		for (UIMouseListener listener: listeners)
			listener.mouseMoved(V.f(event.getX(), event.getY()));
	}

	@Override
	public void onMouseButtonEvent(MouseButtonEvent event) {
		boolean consume = false;

		if (event.getButtonIndex() == 0)
			if (event.isPressed())
				consume = leftButtonPressed(V.f(event.getX(), event.getY()));
			else if (event.isReleased())
				consume = leftButtonReleased(V.f(event.getX(), event.getY()));

		if (consume)
			event.setConsumed();
	}

	@Override
	public void onKeyEvent(KeyInputEvent event) {

	}

	// Unsupported events and redundant methods
	@Override public void beginInput() {}
	@Override public void endInput() {}
	@Override public void onJoyAxisEvent(JoyAxisEvent joyAxisEvent) {}
	@Override public void onJoyButtonEvent(JoyButtonEvent joyButtonEvent) {}
	@Override public void onTouchEvent(TouchEvent touchEvent) {}
	@Override public void write(JmeExporter ex) throws IOException {}
	@Override public void read(JmeImporter im) throws IOException {}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		return this;
	}
}
