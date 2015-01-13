package gui.controls;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import gui.core.Element;
import gui.core.Screen;
import gui.core.V;

public class Label extends Element {
	private int fontSize;
	private String text;
	private BitmapText bitmapText;

	public Label(String id, String text, int fontSize){
		this(text, fontSize, Vector2f.ZERO, Vector2f.ZERO);

		setId(id);
	}

	public Label(String text, int fontSize, Vector2f position, Vector2f size) {
		super(position, size, V.f(0, 0, 1, 1), V.f(0, 0, 0, 0));

		this.text = text;
		this.fontSize = fontSize;
	}

	@Override
	public void initialize(Screen screen){
		super.initialize(screen);

		createText(text, fontSize);
	}

	private void createText(String text, int fontSize) {
		bitmapText = new BitmapText(screen.createFont(), false);

		bitmapText.setBox(new Rectangle(0, 0, getSize().x, getSize().y));
		bitmapText.setLineWrapMode(LineWrapMode.NoWrap);
		bitmapText.setAlignment(BitmapFont.Align.Center);
		bitmapText.setVerticalAlignment(BitmapFont.VAlign.Top);
		bitmapText.setSize(fontSize);
		bitmapText.setColor(ColorRGBA.White);
		bitmapText.setText(text);
		bitmapText.rotate(FastMath.PI, 0, 0);

		attachChild(bitmapText);
	}

	public void setHorizontalAlign(BitmapFont.Align align){
		bitmapText.setAlignment(align);
	}

	public void setVerticalAlign(){

	}

	@Override
	public void setSize(Vector2f size){
		super.setSize(size);

		bitmapText.setBox(new Rectangle(0, 0, getSize().x, getSize().y));
		bitmapText.setLocalTranslation(0, getSize().y, 0);
	}
}
