package shared.items.types;

import shared.items.Item;
import shared.items.ItemTypes;
import shared.map.CardMaster;
import shared.other.DataUtil;

/**
 * @author doc
 */
public class CardItem extends Item {
	private short unitId;

	public CardItem() {
		setType(ItemTypes.CREATURE_CARD);
	}

	public void setUnitId(int unitId) {
		this.unitId = (short) unitId;
	}

	public short getUnitId() {
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
		return DataUtil.shortToByte(unitId);
	}

	@Override
	public void setStats(byte[] data) {
		unitId = DataUtil.byteToShort(data);
	}
}
