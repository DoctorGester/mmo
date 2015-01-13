package core.main.inventory;

import core.main.CardMaster;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public abstract class Item {
	private String name = "Undefined";
	private ItemTier tier = ItemTier.COMMON;
	private int id;
	private byte type;
	private CardMaster owner;

	/** Option ids mapped to their names **/
	public Map<Byte, Short> options = new HashMap<Byte, Short>();

	public final int getOwnerId(){
		CardMaster owner = getOwner();

		return owner == null ? 0 : owner.getId();
	}

	public CardMaster getOwner() {
		return owner;
	}

	public void setOwner(CardMaster owner) {
		this.owner = owner;
	}

	public void setType(int type) {
		this.type = (byte) type;
	}

	public byte getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ItemTier getTier() {
		return tier;
	}

	public void setTier(ItemTier tier) {
		this.tier = tier;
	}

	public final Map<Byte, Short> getOptions(CardMaster cardMaster){
		return options;
	}

	public void addOption(int id, int nameStringId){
		options.put((byte) id, (short) nameStringId);
	}

	public void updateOptions(CardMaster cardMaster){
		options.clear();

		if (cardMaster != null)
			addOptions(cardMaster);
	}

	public abstract void addOptions(CardMaster cardMaster);
	public abstract void onOptionUse(CardMaster cardMaster, int option);
	public abstract byte[] getStats();
	public abstract void setStats(byte data[]);

	public final byte[] toBytes(){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		byte stats[] = getStats();

		try {
			stream.writeByte(getType());
			stream.writeByte(getTier().ordinal());

			stream.writeUTF(getName());

			stream.writeByte(stats.length);
			stream.write(stats);

			stream.write(optionsToBytes(getOwner()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}

	public final byte[] optionsToBytes(CardMaster master){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		Map<Byte, Short> options = getOptions(master);

		try {
			stream.writeByte(options.size());

			for (Map.Entry<Byte, Short> entry: options.entrySet()){
				stream.writeByte(entry.getKey());
				stream.writeShort(entry.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}

	public void fromBytes(byte data[]){
		try {
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
			DataInputStream stream = new DataInputStream(arrayInputStream);

			setType(stream.readByte());
			setTier(ItemTier.values()[stream.readByte()]);

			setName(stream.readUTF());

			byte length = stream.readByte();
			byte stats[] = new byte[length];
			stream.readFully(stats);
			setStats(stats);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Item item = (Item) o;

		return id == item.id;
	}

	@Override
	public int hashCode() {
		return id;
	}
}
