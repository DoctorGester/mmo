package core.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import core.main.ItemDatabase;
import core.ui.battle.BattleUIState;
import program.datastore.Data;
import program.datastore.Subscriber;
import program.main.Program;
import program.main.SceneUtil;
import shared.board.data.CardSpellData;
import shared.items.types.CardItem;
import shared.items.types.SpellCardItem;
import tonegod.gui.controls.text.Label;
import tonegod.gui.core.ElementManager;
import tonegod.gui.listeners.MouseFocusListener;

import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class SpellCardElement extends ItemElement implements MouseFocusListener{
	private SpellCardItem card;
	private CardSpellData spellData;

	public SpellCardElement(ElementManager screen, Vector2f position, float height, SpellCardItem card) {
		super(screen, position, height);
		this.card = card;

		ItemDatabase.getInstance().subscribe(card.getId(), new Subscriber() {
			@Override
			public void receive(String key, Data subscription) {
				SpellCardItem card = subscription.getObject(SpellCardItem.class);
				spellData = Program.getInstance().getCardSpellDataById(card.getSpellId());

				Program.getInstance().getMainFrame().enqueue(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						createElements();

						String description = spellData.getDescription();
						SceneUtil.getUI(UI.STATE_BATTLE, BattleUIState.class).addDescriptionForElement(SpellCardElement.this, description);

						return null;
					}
				});
			}
		});
		ItemDatabase.getInstance().requestItem(card);
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
