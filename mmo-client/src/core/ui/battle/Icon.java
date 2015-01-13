package core.ui.battle;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.core.utils.UIDUtil;

/**
 * @author doc
 */
public class Icon extends Element {
	public Icon(Screen screen, Vector2f position, Vector2f size, String image) {
		super(screen, UIDUtil.getUID(), position, size, new Vector4f(), image);

		setInitialized();
	}
}
