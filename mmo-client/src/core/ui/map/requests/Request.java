package core.ui.map.requests;

import core.main.CardMaster;

public abstract class Request {
	private CardMaster from, to;

	protected Request(CardMaster from, CardMaster to) {
		this.from = from;
		this.to = to;
	}

	public CardMaster getFrom() {
		return from;
	}

	public CardMaster getTo() {
		return to;
	}

	public abstract String getDescriptionOutgoing();
	public abstract String getDescriptionIncoming();
	public abstract void accept();
	public abstract void reject();
	public abstract void cancel();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Request)) return false;

		Request request = (Request) o;

		if (!from.equals(request.from)) return false;
		if (!to.equals(request.to)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = from.hashCode();
		result = 31 * result + to.hashCode();
		return result;
	}
}
