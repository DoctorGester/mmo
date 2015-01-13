package core.main;

public class SpellCard {
	private int id = -1;

	public SpellCard(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean equals(Object object){
		if (!(object instanceof SpellCard))
			return false;

		SpellCard card = (SpellCard) object;
		return (card.id == id);
	}
}
