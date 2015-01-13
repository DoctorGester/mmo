package core.board;

import core.main.CardMaster;

import java.util.LinkedList;
import java.util.List;

public class Alliance {
	private List<CardMaster> alliance = new LinkedList<CardMaster>();
	private int id = -1;

	public List<CardMaster> getAlliance() {
		return alliance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addCardMaster(CardMaster cardMaster){
		alliance.add(cardMaster);
	}
}
