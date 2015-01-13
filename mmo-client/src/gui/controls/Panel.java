package gui.controls;

import com.jme3.math.Vector2f;
import gui.core.ElementContainer;
import gui.core.Screen;
import gui.core.V;

public class Panel extends ElementContainer {
	public Panel(String id){
		this(Vector2f.ZERO, Vector2f.ZERO);

		setId(id);
	}

	public Panel(Vector2f position, Vector2f size) {
		super(position, size, V.f(1, 1, 64, 64), V.f(10, 10, 10, 10));

		initialize(screen);
	}

	public void initialize(Screen screen){
		super.initialize(screen);

		addElement(new Label(getId(), 16, V.f(10, 10), V.f(30, 20)));
	}
}
