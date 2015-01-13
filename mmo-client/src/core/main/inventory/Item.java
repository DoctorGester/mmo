package core.main.inventory;

import core.main.CardMaster;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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

	private boolean initialized = false;

	/** Option ids mapped to their names **/
	public Map<Byte, Short> options = new HashMap<Byte, Short>();

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

	public boolean isInitialized() {
		return initialized;
	}

	public abstract byte[] getStats();
	public abstract void setStats(byte data[]);

	private void readOptions(DataInputStream stream) throws IOException{
		byte optionsSize = stream.readByte();

		for (int i = 0; i < optionsSize; i++){
			byte id = stream.readByte();
			short name = stream.readShort();

			options.put(id, name);
		}
	}

	public void fromBytes(DataInputStream stream){
		try {
			setType(stream.readByte());
			setTier(ItemTier.values()[stream.readByte()]);

			setName(stream.readUTF());

			byte length = stream.readByte();
			byte stats[] = new byte[length];
			stream.readFully(stats);
			setStats(stats);

			readOptions(stream);

			initialized = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void fromBytes(byte data[]){
		ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
		fromBytes(new DataInputStream(arrayInputStream));
	}
}
