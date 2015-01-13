package gui.controls;

import com.jme3.math.Vector2f;
import gui.core.*;

public class Button extends ElementContainer implements UIMouseListener {
	private static final float BLEND_SPEED = 6;
	private boolean pressed = false;
	private boolean hover = false;
	private float pressedBlendStep = 0f, hoverBlendStep = 0f;

	private Label label;

	public Button(String id){
		this(Vector2f.ZERO, Vector2f.ZERO);

		setId(id);
	}

	public Button(Vector2f position, Vector2f size) {
		super(position, size, V.f(153, 1, 16, 16), V.f(4, 4, 4, 4));

		enableBlend(true);
		setBlendStep(0f);
		hoverOffset();
	}

	@Override
	public void initialize(Screen screen){
		super.initialize(screen);

		screen.addMouseListener(this);
	}

	public Button setText(String text, int fontSize){
		if (label == null){
			//label = new Label(screen, text, fontSize, new CC().grow());

			//addElement(label);
			//addElement(new Panel(screen, new CC().grow()));

			//getLayout().setLayoutConstraints(new LC().fillX()).layout();
		}

		return this;
	}

	@Override
	public void leftButtonPressed(Vector2f point) {
		pressed = true;
		setBlendImageOffset(V.f(153, 37));
	}

	@Override
	public void leftButtonReleased(Element target, Vector2f point) {
		if (pressed && target == this){
			// TODO add some actions
		}

		hoverOffset();
		pressed = false;
	}

	@Override
	public void mouseMoved(Vector2f point) {
		hover = contains(point);
	}

	@Override
	public Element getElement() {
		return this;
	}

	@Override
	public void update(float tpf){
		if (pressed)
			pressedBlendStep = Math.min(1.0f, pressedBlendStep + tpf * BLEND_SPEED);
		else
			pressedBlendStep = Math.max(0.0f, pressedBlendStep - tpf * BLEND_SPEED);

		if (hover)
			hoverBlendStep = Math.min(1.0f, hoverBlendStep + tpf * BLEND_SPEED);
		else
			hoverBlendStep = Math.max(0.0f, hoverBlendStep - tpf * BLEND_SPEED);

		if (pressed)
			setBlendStep(pressedBlendStep);
		else
			setBlendStep(hoverBlendStep);
	}

	@Override
	public void setSize(Vector2f size) {
		super.setSize(size);
	}

	private void hoverOffset(){
		setBlendImageOffset(V.f(153, 19));
	}
}
