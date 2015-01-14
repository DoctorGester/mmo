package core.handlers;

import core.board.ServerBoard;
import core.board.TurnManager;
import core.main.*;
import program.main.Program;
import shared.board.Board;
import shared.board.Cell;
import shared.map.CardMaster;

public class BattlePlaceMessageHandler extends PacketHandler {
	private Program program;

	public BattlePlaceMessageHandler(byte header[]) {
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

		// Special case where the board is in the placement state
		if (board.getState() != Board.STATE_WAIT_FOR_PLACEMENT)
			return;

		Cell selected = board.getCellChecked(data[0], data[1]),
			 target = board.getCellChecked(data[2], data[3]);

		TurnManager.getInstance().place(board, cm, selected, target);
	}

	public void handle(LocalClient localClient, Packet data) {}
}
