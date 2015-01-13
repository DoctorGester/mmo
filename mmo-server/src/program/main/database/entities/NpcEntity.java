package program.main.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "npcPlayers")
public class NpcEntity {
	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private CardMasterEntity cardMaster;

	public int getId() {
		return id;
	}

	public CardMasterEntity getCardMaster() {
		return cardMaster;
	}

	public void setCardMaster(CardMasterEntity cardMaster) {
		this.cardMaster = cardMaster;
	}
}
