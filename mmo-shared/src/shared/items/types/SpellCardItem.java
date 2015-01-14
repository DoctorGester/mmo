package shared.items.types;

import shared.items.Item;
import shared.items.ItemTypes;
import shared.map.CardMaster;
import shared.other.DataUtil;

/**
 * @author doc
 */
public class SpellCardItem extends Item {
	private String spellId;

	public SpellCardItem() {
		setType(ItemTypes.SPELL_CARD);
	}

	public String getSpellId() {
		return spellId;
	}

	public void setSpellId(String spellId) {
		this.spellId = spellId;
	}

	@Override
	public void addOptions(CardMaster cardMaster) {}

	@Override
	public void onOptionUse(CardMaster cardMaster, int option) {}

	@Override
	public byte[] getStats() {
		return spellId.getBytes(DataUtil.UTF_8);
	}

	@Override
	public void setStats(byte[] data) {
		spellId = new String(data, DataUtil.UTF_8);
	}
}
