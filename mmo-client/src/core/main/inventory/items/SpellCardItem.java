package core.main.inventory.items;

import core.main.inventory.Item;
import core.main.inventory.ItemTypes;
import program.main.Program;

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
	public byte[] getStats() {
		return spellId.getBytes(Program.UTF_8);
	}

	@Override
	public void setStats(byte[] data) {
		spellId = new String(data, Program.UTF_8);
	}
}
