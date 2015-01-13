package program.main.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "matches")
public class MatchEntity {
	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "LONGVARBINARY NOT NULL")
	private byte[] setup;

	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "LONGVARBINARY NOT NULL")
	private byte[] participants;

	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "LONGVARBINARY")
	private byte[] replay;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public byte[] getSetup() {
		return setup;
	}

	public void setSetup(byte[] setup) {
		this.setup = setup;
	}

	public byte[] getParticipants() {
		return participants;
	}

	public void setParticipants(byte[] participants) {
		this.participants = participants;
	}

	public byte[] getReplay() {
		return replay;
	}

	public void setReplay(byte[] replay) {
		this.replay = replay;
	}
}
