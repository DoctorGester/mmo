package core.handlers;

import core.board.ServerBoard;
import core.board.TurnManager;
import core.main.*;
import program.main.Program;
import shared.board.Board;
import shared.board.Cell;
import shared.map.CardMaster;

public class CastSpellMessageHandler extends PacketHandler {
	private Program program;

	public CastSpellMessageHandler(byte header[]) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		GameClient gameClient = program.findClient(client);

		// Exit if client is missing or not in the battle state
		if (gameClient == null || gameClient.getCardMaster().getState() != CardMaster.STATE_IN_BATTLE)
			return;

		byte data[] = packet.getData();

		if (data.length != 5) // 1 byte for spell number, 4 bytes for cords
			return;

		ServerCardMaster cm = gameClient.getCardMaster();
		ServerBoard board = cm.getCurrentBoard();
		// Board has to be in the wait for order state
		if (board.getState() != Board.STATE_WAIT_FOR_ORDER)
			return;

		Cell selected = board.getCellChecked(data[1], data[2]),
			 target = board.getCellChecked(data[3], data[4]);

		TurnManager.getInstance().cast(board, cm, data[0], selected, target);
	}

	public void handle(LocalClient localClient, Packet data) {
	}

}
