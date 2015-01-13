package core.main;

import core.main.inventory.Item;

import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class TradePartner {
	private CardMaster client;
	private final List<Item> offering;
	private boolean hasAuthorizedTrade,
					hasRequestedToCompleteTrade;

	public TradePartner(CardMaster client) {
		offering = new LinkedList<Item>();
		this.client = client;
	}

	public CardMaster getClient() {
		return client;
	}

	public List<Item> getOffering() {
		return offering;
	}

	public boolean hasAuthorizedTrade() {
		return hasAuthorizedTrade;
	}

	public void authorizeTrade(boolean authorize){
		hasAuthorizedTrade = authorize;
	}

	public void completeTrade(boolean complete){
		hasRequestedToCompleteTrade = complete;
	}

	public boolean hasRequestedToCompleteTrade() {
		return hasRequestedToCompleteTrade;
	}

	public void offerItem(Item item){
		synchronized (offering){
			offering.add(item);
		}
	}
}
