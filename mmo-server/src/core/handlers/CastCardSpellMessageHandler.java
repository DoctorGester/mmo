package core.handlers;

import core.board.ServerBoard;
import core.board.TurnManager;
import core.main.*;
import program.main.Program;
import shared.board.Board;
import shared.map.CardMaster;
import shared.other.DataUtil;

public class CastCardSpellMessageHandler extends PacketHandler {
	private Program program;

	public CastCardSpellMessageHandler(byte header[]) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		GameClient gameClient = program.findClient(client);

		// Exit if client is missing or not in the battle state
		if (gameClient == null || gameClient.getCardMaster().getState() != CardMaster.STATE_IN_BATTLE)
			return;

		byte data[] = packet.getData();

		if (data.length != 4)
			return;

		ServerCardMaster cm = gameClient.getCardMaster();
		ServerBoard board = cm.getCurrentBoard();

		// Board has to be in the wait for order state
		if (board.getState() != Board.STATE_WAIT_FOR_ORDER)
			return;

		TurnManager.getInstance().castCard(board, cm, DataUtil.byteToInt(data));
	}

	public void handle(LocalClient localClient, Packet data) {
	}

}
