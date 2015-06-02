package core.ui.deck;

import program.main.Program;
import shared.items.Item;
import shared.items.types.SpellCardItem;
import shared.items.types.UnitCardItem;

/**
 * @author doc
 */
public class CardModelFactory {
	public static CardModel createModel(Item item, float size){
		if (item instanceof UnitCardItem){
			String unitId = ((UnitCardItem) item).getUnitId();
			return new UnitCardModel(Program.getInstance().getUnitDataById(unitId), size);
		}

		if (item instanceof SpellCardItem){
			return null;
		}

		throw new IllegalArgumentException("Unsupported item type");
	}
}
