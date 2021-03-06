package core.main;

import shared.map.CardMaster;

/**
 * @author doc
 */
public class DuelRequest {
	public static final int STATUS_PENDING = 0x00,
							STATUS_ACCEPTED = 0x01,
							STATUS_REJECTED = 0x02,
							STATUS_TIMED_OUT = 0x03,
							STATUS_CANCELLED = 0x04;

	private static final float TIMEOUT = 500f;

	private ServerCardMaster sender, target;
	private float timeLeft = TIMEOUT;
	private int status = STATUS_PENDING;

	public DuelRequest(ServerCardMaster sender, ServerCardMaster target) {
		this.sender = sender;
		this.target = target;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public ServerCardMaster getTarget() {
		return target;
	}

	public ServerCardMaster getSender() {
		return sender;
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
