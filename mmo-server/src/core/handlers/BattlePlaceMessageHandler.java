package core.handlers;

import core.board.TurnManager;
import core.board.ai.AIManager;
import core.board.interfaces.Board;
import core.board.interfaces.Cell;
import core.main.*;
import core.main.inventory.items.CardItem;
import program.main.Program;

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

		CardMaster cm = gameClient.getCardMaster();
		Board board = cm.getCurrentBoard();

		// Special case where the board is in the placement state
		if (board.getState() != Board.STATE_WAIT_FOR_PLACEMENT)
			return;

		Cell selected = board.getCellChecked(data[0], data[1]),
			 target = board.getCellChecked(data[2], data[3]);

		TurnManager.getInstance().place(board, cm, selected, target);
	}

	public void handle(LocalClient localClient, Packet data) {}
}
