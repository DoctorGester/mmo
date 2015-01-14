package core.main;

import shared.items.Item;
import shared.map.CardMaster;

import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class TradePartner {
	private ServerCardMaster client;
	private final List<Item> offering;
	private boolean hasAuthorizedTrade,
					hasRequestedToCompleteTrade;

	public TradePartner(ServerCardMaster client) {
		offering = new LinkedList<Item>();
		this.client = client;
	}

	public ServerCardMaster getClient() {
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
