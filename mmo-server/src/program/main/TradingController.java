package program.main;

import core.main.*;
import shared.items.Item;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TradingController {
	private final List<Trade> trades = new LinkedList<Trade>();

	private Trade findTrade(CardMaster client){
		synchronized (trades){
			for (Trade trade: trades)
				if (trade.getInitiator().getClient() == client)
					return trade;
		}
		return null;
	}

	private Trade findTradeWide(CardMaster client){
		synchronized (trades){
			for (Trade trade: trades)
				if (trade.getPartner(client) != null)
					return trade;
		}
		return null;
	}

	public void request(ServerCardMaster from, ServerCardMaster to){
		System.out.println("rq " + from.getName() + " " + to.getName());
		if (from == to)
			return;

		synchronized (trades){
			Trade found = findTrade(from);

			if (found != null)
				return;

			found = findTrade(to);

			if (found != null && found.getTarget().getClient() == from){
				found.setStatus(Trade.STATUS_ACCEPTED);
				return;
			}

			Trade trade = new Trade(from, to);
			trades.add(trade);

			byte arr[] = DataUtil.intToVarInt(from.getId(), to.getId());
			send(trade, Program.HEADER_REQUEST_TRADE, arr);
		}
	}

	public void cancel(CardMaster initiator){
		System.out.println("cn " + initiator.getName());
		Trade trade = findTrade(initiator);

		if (trade == null)
			return;

		trade.setStatus(Trade.STATUS_CANCELLED);
	}

	public void reject(CardMaster initiator){
		System.out.println("rj " + initiator.getName());
		Trade trade = findTrade(initiator);

		if (trade == null)
			return;

		trade.setStatus(Trade.STATUS_REJECTED);
	}

	public void accept(CardMaster initiator){
		System.out.println("ac " + initiator.getName());
		Trade trade = findTrade(initiator);

		if (trade == null)
			return;

		trade.setStatus(Trade.STATUS_ACCEPTED);
	}

	public void offerItem(CardMaster owner, int itemId){
		System.out.println("of " + owner.getName() + " " + itemId);
		Trade trade = findTradeWide(owner);

		if (trade == null)
			return;

		if (trade.getStatus() != Trade.STATUS_WAITING)
			return;

		TradePartner partner = trade.getPartner(owner);
		TradePartner secondPartner = trade.getOtherPartner(partner);

		if (partner.hasAuthorizedTrade())
			return;

		Item item = owner.getInventory().findById(itemId);

		if (item == null)
			return;

		partner.offerItem(item);

		if (secondPartner.hasAuthorizedTrade())
			authorizeTrade(secondPartner.getClient(), false);

		send(trade, Program.HEADER_TRADE_OFFER_ITEM, DataUtil.intToVarInt(owner.getId(), itemId));
	}

	public void authorizeTrade(CardMaster owner, boolean authorize){
		System.out.println("au " + owner.getName() + " " + authorize);
		Trade trade = findTradeWide(owner);

		if (trade == null)
			return;

		if (trade.getStatus() != Trade.STATUS_WAITING)
			return;

		TradePartner partner = trade.getPartner(owner);
		TradePartner secondPartner = trade.getOtherPartner(partner);

		if (!authorize && secondPartner.hasRequestedToCompleteTrade())
			authorizeTrade(secondPartner.getClient(), false);

		partner.authorizeTrade(true);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeInt(owner.getId());
			stream.writeBoolean(authorize);
		} catch (IOException e) {
			e.printStackTrace();
		}

		send(trade, Program.HEADER_AUTHORIZE_TRADE, bytes.toByteArray());
	}

	public void completeTrade(CardMaster owner, boolean complete){
		Trade trade = findTradeWide(owner);

		if (trade == null)
			return;

		if (trade.getStatus() != Trade.STATUS_WAITING)
			return;

		if (trade.getInitiator().hasAuthorizedTrade() && trade.getTarget().hasAuthorizedTrade()){
			TradePartner partner = trade.getPartner(owner);

			partner.completeTrade(true);

			if (trade.getInitiator().hasRequestedToCompleteTrade() && trade.getTarget().hasRequestedToCompleteTrade()){
				trade.setStatus(Trade.STATUS_COMPLETED);
			}
		}
	}

	private void performTrade(Trade trade){
		List<Item> offer = trade.getInitiator().getOffering();
		List<Item> response = trade.getTarget().getOffering();

		trade.getInitiator().getClient().getInventory().removeItems(offer).addItems(response);
		trade.getTarget().getClient().getInventory().removeItems(response).addItems(offer);
	}

	private void send(Trade trade, byte header[], byte data[]){
		send(trade.getInitiator().getClient(), header, data);
		send(trade.getTarget().getClient(), header, data);
	}

	private void send(CardMaster player, byte header[], byte data[]){
		GameClient gameClient = Program.getInstance().getGameClientByCardMaster(player);

		if (gameClient != null)
			ReliablePacketManager.sendPacket(Program.getInstance().localServer, gameClient.getClient(), header, data);
	}

	public void update(){
		synchronized (trades){
			for (Iterator<Trade> iterator = trades.iterator(); iterator.hasNext(); ){
				Trade trade = iterator.next();

				trade.updateTime();

				byte senderId[] = DataUtil.intToByte(trade.getInitiator().getClient().getId());

				if (trade.getInitiator().getClient().getState() != CardMaster.STATE_IN_GLOBAL_MAP)
					trade.setStatus(Trade.STATUS_CANCELLED);

				if (trade.getTarget().getClient().getState() != CardMaster.STATE_IN_GLOBAL_MAP)
					trade.setStatus(Trade.STATUS_REJECTED);

				if (trade.getStatus() == Trade.STATUS_ACCEPTED){
					trade.setStatus(Trade.STATUS_WAITING);
					send(trade, Program.HEADER_ACCEPT_TRADE, senderId);
				}

				if (trade.getStatus() == Trade.STATUS_PENDING || trade.getStatus() == Trade.STATUS_WAITING)
					continue;

				iterator.remove();

				switch (trade.getStatus()){
					case Trade.STATUS_TIMED_OUT:
						send(trade, Program.HEADER_TRADE_TIMEOUT, senderId);
						break;
					case Trade.STATUS_REJECTED:
						send(trade, Program.HEADER_REJECT_TRADE, senderId);
						break;
					case Trade.STATUS_CANCELLED:
						send(trade, Program.HEADER_CANCEL_TRADE, senderId);
						break;
					case Trade.STATUS_COMPLETED:
						performTrade(trade);
						break;
				}
			}
		}
	}
}
