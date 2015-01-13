package program.main.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cardMasters")
public class CardMasterEntity {
	public static final String NAME_FIELD = "name";

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false, unique = true)
	private String name;

	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "LONGVARBINARY NOT NULL")
	private byte[] data;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
