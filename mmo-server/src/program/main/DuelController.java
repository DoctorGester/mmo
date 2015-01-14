package program.main;

import shared.board.BoardSetup;
import core.main.*;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DuelController {
	private final List<DuelRequest> requests = new LinkedList<DuelRequest>();

	private DuelRequest findRequest(CardMaster player){
		synchronized (requests){
			for (DuelRequest request: requests){
				if (request.getSender() == player)
					return request;
			}
		}
		return null;
	}

	public void request(ServerCardMaster from, ServerCardMaster to){
		if (from == to)
			return;

		synchronized (requests){
			DuelRequest found = findRequest(from);

			if (found != null)
				return;

			found = findRequest(to);

			if (found != null && found.getTarget() == from){
				found.setStatus(DuelRequest.STATUS_ACCEPTED);
				return;
			}

			DuelRequest request = new DuelRequest(from, to);
			requests.add(request);

			byte arr[] = DataUtil.intToVarInt(from.getId(), to.getId());
			send(request, Program.HEADER_REQUEST_DUEL, arr);
		}
	}

	public void cancel(CardMaster initiator){
		DuelRequest request = findRequest(initiator);

		if (request == null)
			return;

		request.setStatus(DuelRequest.STATUS_CANCELLED);
	}

	public void reject(CardMaster initiator){
		DuelRequest request = findRequest(initiator);

		if (request == null)
			return;

		request.setStatus(DuelRequest.STATUS_REJECTED);
	}

	public void accept(CardMaster initiator){
		DuelRequest request = findRequest(initiator);

		if (request == null)
			return;

		request.setStatus(DuelRequest.STATUS_ACCEPTED);
	}

	private void startBattle(DuelRequest request){
		BoardSetup duelSetup = new BoardSetup()
									.setWidth(8)
									.setHeight(8)
									.addPlacementArea(new Rectangle(1, 0, 5, 2))
									.addPlacementArea(new Rectangle(1, 6, 5, 2))
									.addAlliance(new Integer[]{0})
									.addAlliance(new Integer[]{1})
									.setShuffle(true)
									.setTurnTime(60f);

		Program.getInstance().getBattleController().startBattle(duelSetup,
																request.getSender(),
																request.getTarget());
	}

	private void send(DuelRequest request, byte header[], byte data[]){
		Program program = Program.getInstance();

		LocalServer server = program.localServer;
		GameClient senderClient = program.getGameClientByCardMaster(request.getSender());
		GameClient targetClient = program.getGameClientByCardMaster(request.getTarget());

		if (senderClient != null)
			ReliablePacketManager.sendPacket(server, senderClient.getClient(), header, data);

		if (targetClient != null)
			ReliablePacketManager.sendPacket(server, targetClient.getClient(), header, data);
	}

	public void update(){
		synchronized (requests){
			for (Iterator<DuelRequest> iterator = requests.iterator(); iterator.hasNext(); ){
				DuelRequest request = iterator.next();

				request.updateTime();

				if (request.getSender().getState() != CardMaster.STATE_IN_GLOBAL_MAP)
					request.setStatus(DuelRequest.STATUS_CANCELLED);

				if (request.getTarget().getState() != CardMaster.STATE_IN_GLOBAL_MAP)
					request.setStatus(DuelRequest.STATUS_REJECTED);

				if (request.getStatus() == DuelRequest.STATUS_PENDING)
					continue;

				iterator.remove();

				byte senderId[] = DataUtil.intToByte(request.getSender().getId());

				switch (request.getStatus()){
					case DuelRequest.STATUS_TIMED_OUT:
						send(request, Program.HEADER_DUEL_TIMEOUT, senderId);
						break;
					case DuelRequest.STATUS_REJECTED:
						send(request, Program.HEADER_REJECT_DUEL, senderId);
						break;
					case DuelRequest.STATUS_CANCELLED:
						send(request, Program.HEADER_CANCEL_DUEL, senderId);
						break;
					case DuelRequest.STATUS_ACCEPTED:
						startBattle(request);
				}
			}
		}
	}
}
