package shared.items.types;

import shared.items.Item;
import shared.items.ItemTypes;
import shared.map.CardMaster;
import shared.other.DataUtil;

/**
 * @author doc
 */
public class UnitCardItem extends Item {
	private String unitId;

	public UnitCardItem() {
		setType(ItemTypes.CREATURE_CARD);
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getUnitId() {
		return unitId;
	}

	@Override
	public void addOptions(CardMaster cardMaster) {
		addOption(0, 0);
		addOption(1, 3);
	}

	@Override
	public void onOptionUse(CardMaster cardMaster, int option) {
		System.out.println(cardMaster.getName() + " used opt " + option + " on item " + getId());
	}

	@Override
	public byte[] getStats() {
		return unitId.getBytes(DataUtil.UTF_8);
	}

	@Override
	public void setStats(byte[] data) {
		unitId = new String(data, DataUtil.UTF_8);
	}
}
