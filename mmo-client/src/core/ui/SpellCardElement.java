package core.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import core.board.CardSpellData;
import core.main.inventory.items.SpellCardItem;
import program.main.Program;
import tonegod.gui.controls.text.Label;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.Screen;
import tonegod.gui.listeners.MouseFocusListener;

/**
 * @author doc
 */
public class SpellCardElement extends ItemElement implements MouseFocusListener{
	private SpellCardItem card;
	private CardSpellData spellData;

	public SpellCardElement(ElementManager screen, Vector2f position, float height, SpellCardItem card) {
		super(screen, position, height);

		this.spellData = Program.getInstance().getCardSpellDataById(card.getSpellId());
		this.card = card;

		createElements();
	}

	public SpellCardElement(ElementManager screen, Vector2f position, float height, CardSpellData spellData) {
		super(screen, position, height);
		this.spellData = spellData;
		this.card = null;

		createElements();
	}

	private void createElements(){
		float height = getHeight() * 0.40f;

		Vector2f labelPosition = new Vector2f(0, height);
		Vector2f labelSize = new Vector2f(getWidth(), getHeight() - height);

		Label name = new Label(screen, labelPosition, labelSize);
		name.setText(spellData.getName());
		name.setFontColor(ColorRGBA.Brown);
		name.setTextAlign(BitmapFont.Align.Center);
		name.setTextVAlign(BitmapFont.VAlign.Center);
		name.setTextWrap(LineWrapMode.Word);
		name.setIgnoreMouse(true);
		name.setFontSize(getHeight() * 0.15f);

		addFrontElement(name);
	}

	public SpellCardItem getCard() {
		return card;
	}

	public CardSpellData getSpellData() {
		return spellData;
	}

	public void onGetFocus(MouseMotionEvent evt) {
		setHasFocus(true);
	}

	public void onLoseFocus(MouseMotionEvent evt) {
		setHasFocus(false);
	}
}
