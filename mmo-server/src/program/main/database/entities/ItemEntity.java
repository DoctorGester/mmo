package program.main.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "items")
public class ItemEntity {
	public static final String OWNER_ID_FIELD = "owner_id";

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false)
	private String type;

	@DatabaseField(foreign = true)
	private CardMasterEntity owner;

	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "LONGVARBINARY NOT NULL")
	private byte[] data;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CardMasterEntity getOwner() {
		return owner;
	}

	public void setOwner(CardMasterEntity owner) {
		this.owner = owner;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
