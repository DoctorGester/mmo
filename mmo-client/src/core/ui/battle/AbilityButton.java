package core.ui.battle;

import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import core.ui.BattleState;
import core.ui.UI;
import program.main.Program;
import program.main.Util;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.core.Screen;

/**
 * @author doc
 */
public class AbilityButton extends ButtonAdapter{
	public static final String SPELL_KEY = "Spell";

	public static final float OFF_COOLDOWN = -1f;
	private float from = OFF_COOLDOWN, to = OFF_COOLDOWN, step = 1f, current = from;

	private boolean active = true;
	private boolean clicked = false;
	private boolean glowing;

	public AbilityButton(Screen screen, Vector2f position, Vector2f size, String image) {
		super(screen, position, size);
		setLocalMaterial(new Material(screen.getApplication().getAssetManager(), "res/shaders/CoolDownButton.j3md"));
		setColorMap(image);
		setButtonPressedInfo(image, ColorRGBA.White);
		setButtonHoverInfo(image, ColorRGBA.White);
		setCoolDownProgressImmediate(OFF_COOLDOWN);
		setTextAlign(BitmapFont.Align.Center);
		setTextVAlign(BitmapFont.VAlign.Center);
		setFontSize(36);
		setFont("res/other/segoe_outlined.fnt");
	}

	public void setCoolDownProgress(float remaining){
		if (remaining == 1f){
			setCoolDownProgressImmediate(1f);
		} else {
			from = current;
			to = remaining;
			step = 0;
		}
	}

	public void setCoolDownProgressImmediate(float remaining){
		from = remaining;
		to = remaining;
		current = remaining;
		step = 1;
		getMaterial().setFloat("CoolDownLeft", 1 - (remaining * 2));
	}

	public void updateCooldownProgress(){
		step = Math.min(1, step + 0.03f);
		current = FastMath.interpolateLinear(step, from, to);
		if (current < 0.01f)
			setCoolDownProgressImmediate(-1);
		else
			getMaterial().setFloat("CoolDownLeft", 1 - (current * 2));
	}

	public void setTurnsLeft(int turns){
		if (turns == 0)
			setText("");
		else
			setText(String.valueOf(turns));
	}

	public void setActive(boolean active) {
		this.active = active;
		getMaterial().setBoolean("Active", active);
	}

	public void onButtonMouseLeftDown(MouseButtonEvent evt, boolean toggled) {
		if (active && current == OFF_COOLDOWN){
			getMaterial().setBoolean("Pressed", true);
			clicked = true;
		}
	}

	public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
		getMaterial().setBoolean("Pressed", false);
		if (clicked){
			BattleState battleState = Util.getUI(UI.STATE_BATTLE, BattleUIState.class).getBattleState();
			Program.getInstance().getBattleController().abilityButtonClicked(battleState, (Integer) getUserData(SPELL_KEY));
			clicked = false;
		}
	}

	public void setGlowing(boolean glowing) {
		getMaterial().setBoolean("Glowing", glowing);
	}
}
