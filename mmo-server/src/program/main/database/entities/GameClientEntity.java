package program.main.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "gameClients")
public class GameClientEntity {
	public static final String PASSWORD_FIELD = "password";

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField(canBeNull = false)
	private String password;

	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private CardMasterEntity cardMaster;

	public int getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public CardMasterEntity getCardMaster() {
		return cardMaster;
	}

	public void setCardMaster(CardMasterEntity cardMaster) {
		this.cardMaster = cardMaster;
	}
}
