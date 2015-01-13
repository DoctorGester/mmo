package core.ui.battle;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.core.utils.UIDUtil;

/**
 * @author doc
 */
public class BannerElement extends Element {
	private float angle;

	public BannerElement(Screen screen, Vector2f position, Vector2f size, final ColorRGBA color, String playerName) {
		super(screen, UIDUtil.getUID(), position, size, new Vector4f(), "res/textures/banner.png");

		setLocalMaterial(new Material(screen.getApplication().getAssetManager(), "res/shaders/Banner.j3md"));
		setColorMap("res/textures/banner.png");
		getMaterial().setColor("Back", ColorRGBA.LightGray);
		getMaterial().setColor("Front", color);
		getMaterial().setTexture("Mask", screen.getApplication().getAssetManager().loadTexture("res/textures/banner-mask.png"));
		getMaterial().getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
}
