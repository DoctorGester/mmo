package core.ui;

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import core.main.ItemDatabase;
import program.datastore.Data;
import program.datastore.Subscriber;
import program.main.Program;
import program.main.data.ClientDataLoader;
import shared.board.data.UnitData;
import shared.items.types.CardItem;
import tonegod.gui.controls.text.Label;
import tonegod.gui.core.ElementManager;

import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class UnitCardElement extends ItemElement {
	private UnitData unitData;

	private CardItem card;
	private int cardId;
	private PortraitElement portrait;
	private Label name;

	public UnitCardElement(ElementManager screen, Vector2f position, float height, CardItem card) {
		super(screen, position, height);

		this.card = card;
		this.cardId = card.getId();

		ItemDatabase.getInstance().subscribe(card.getId(), new Subscriber() {
			@Override
			public void receive(String key, Data subscription) {
				CardItem card = subscription.getObject(CardItem.class);
				unitData = Program.getInstance().getUnitDataById(card.getUnitId());

				Program.getInstance().getMainFrame().enqueue(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						createPortrait(unitData);

						addFrontElement(portrait);
						addFrontElement(name);
						return null;
					}
				});
			}
		});
		ItemDatabase.getInstance().requestItem(card);
	}

	public UnitCardElement(ElementManager screen, Vector2f position, float height, UnitData unitData) {
		super(screen, position, height);
		this.unitData = unitData;

		this.card = null;
		this.cardId = -1;

		createPortrait(unitData);

		addFrontElement(portrait);
		addFrontElement(name);
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public int getCardId() {
		return cardId;
	}

	public CardItem getCard() {
		return card;
	}

	private void createPortrait(UnitData unitData){
		float height = getHeight() * 0.80f;
		float paddingBot = getHeight() * 0.15f;

		Vector3f cameraPosition = new Vector3f(5, 5, 5);
		Vector3f cameraTarget = new Vector3f(0, 3, 0);
		portrait = new PortraitElement(screen, unitData.getName(), height, ClientDataLoader.getUnitModel(unitData), cameraPosition, cameraTarget);
		portrait.setInitialized();

		float width = portrait.getWidth();
		float paddingRight = (getWidth() - width) / 2f;

		portrait.setPosition(paddingRight, paddingBot);

		Vector2f labelPosition = new Vector2f(0, height);
		Vector2f labelSize = new Vector2f(getWidth(), getHeight() - height);

		name = new Label(screen, labelPosition, labelSize);
		name.setText(unitData.getName());
		name.setFontColor(ColorRGBA.Brown);
		name.setTextAlign(BitmapFont.Align.Center);
		name.setFontSize(getHeight() * 0.15f);
	}

	public UnitData getUnitData() {
		return unitData;
	}

}
