package core.ui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;

import java.util.Iterator;
import java.util.Map;

/**
 * @author doc
 */
public class CloseablePanel extends Panel {
	private static final float BUTTON_SIZE = 0.025f,
							   OFFSET = 0.35f;

	private Button closeButton;

	public CloseablePanel(ElementManager screen, Vector2f position, Vector2f dimensions) {
		super(screen, position, dimensions);

		createCloseButton();
	}

	private void createCloseButton(){
		float side = screen.getWidth() * BUTTON_SIZE;
		float offset = side * OFFSET;

		Vector2f buttonPosition = new Vector2f(getWidth() - side - offset, offset);
		Vector2f buttonSize = new Vector2f(side, side);

		closeButton = new ButtonAdapter(screen, buttonPosition, buttonSize){
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				onClose();
			}
		};

		closeButton.setButtonIcon(buttonSize.x * 0.7f, buttonSize.y * 0.7f, "res/textures/interface/reject.png");

		addChild(closeButton);
	}

	public void removeAllChildren(){
		Iterator<Map.Entry<String, Element>> iterator = elementChildren.entrySet().iterator();

		while(iterator.hasNext()){
			Map.Entry<String, Element> entry = iterator.next();

			if (entry.getValue() != closeButton) {
				entry.getValue().removeFromParent();
				iterator.remove();
			}
		}
	}

	public void onClose(){
		setIsVisible(false);
	}
}
