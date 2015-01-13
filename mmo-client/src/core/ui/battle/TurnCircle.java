package core.ui.battle;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.core.Screen;

/**
 * @author doc
 */
public class TurnCircle extends ButtonAdapter {
	public TurnCircle(Screen screen, Vector2f position, Vector2f dimensions) {
		super(screen, position, dimensions);

		setLocalMaterial(new Material(screen.getApplication().getAssetManager(), "res/shaders/TurnCircle.j3md"));
		//getMaterial().getMaterialDef().addMaterialParam(VarType.Vector4Array, "Colors", new ColorRGBA[0], null);
	}

	public void setup(ColorRGBA colors[]){
		Vector4f vectors[] = new Vector4f[colors.length];

		for(int i = 0; i < colors.length; i++)
			vectors[i] = colors[i].toVector4f();

		getMaterial().setParam("Colors", VarType.Vector4Array, vectors);
		getMaterial().setInt("PlayerAmount", colors.length);
	}

	public void setStep(int turningPlayer, float turnProgress){
		getMaterial().setInt("TurningPlayer", turningPlayer);
		getMaterial().setFloat("TurnProgress", turnProgress);
	}

	public void onButtonMouseLeftUp(com.jme3.input.event.MouseButtonEvent evt, boolean toggled) {
		((DesktopAssetManager) app.getAssetManager()).clearCache();
		setLocalMaterial(new Material(getScreen().getApplication().getAssetManager(), "res/shaders/TurnCircle.j3md"));
	}
}
