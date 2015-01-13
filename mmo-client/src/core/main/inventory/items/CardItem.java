package core.main.inventory.items;

import core.main.DataUtil;
import core.main.inventory.Item;
import core.main.inventory.ItemTypes;

/**
 * @author doc
 */
public class CardItem extends Item {
	private short unitId;

	public CardItem() {
		setType(ItemTypes.UNIT_CARD);
	}

	public void setUnitId(int unitId) {
		this.unitId = (short) unitId;
	}

	public short getUnitId() {
		return unitId;
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
