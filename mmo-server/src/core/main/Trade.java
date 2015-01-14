package core.main;

import shared.map.CardMaster;

/**
 * @author doc
 */
public class Trade {
	public static final int STATUS_PENDING = 0x00,
							STATUS_ACCEPTED = 0x01,
							STATUS_REJECTED = 0x02,
							STATUS_TIMED_OUT = 0x03,
							STATUS_CANCELLED = 0x04,
							STATUS_WAITING = 0x05,
							STATUS_COMPLETED = 0x06;

	private static final float TIMEOUT = 500f;

	private TradePartner initiator, target;
	private float timeLeft = TIMEOUT;
	private int status = STATUS_PENDING;

	public Trade(ServerCardMaster initiator, ServerCardMaster target) {
		this.initiator = new TradePartner(initiator);
		this.target = new TradePartner(target);
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public TradePartner getTarget() {
		return target;
	}

	public TradePartner getInitiator() {
		return initiator;
	}

	public TradePartner getPartner(CardMaster client){
		if (target.getClient() == client)
			return target;

		if (initiator.getClient() == client)
			return initiator;

		return null;
	}

	public TradePartner getOtherPartner(TradePartner partner){
		if (partner == target)
			return initiator;

		if (partner == initiator)
			return target;

		return null;
	}

	public void resetTime(){
		timeLeft = TIMEOUT;
	}

	public void updateTime(){
		if (status != STATUS_PENDING)
			return;

		if (timeLeft > 0f)
			timeLeft -= 0.01f;
		else
			status = STATUS_TIMED_OUT;
	}
}
