package core.handlers;

import core.main.*;
import program.main.Program;
import shared.board.Board;
import shared.map.CardMaster;

public class PlacementFinishedMessageHandler extends PacketHandler {
	private Program program;

	public PlacementFinishedMessageHandler(byte[] header) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalClient localClient, Packet packet) {}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		GameClient gameClient = program.findClient(client);

		// Exit if client is missing or not in the battle state
		if (gameClient == null)
			return;

		CardMaster cm = gameClient.getCardMaster();

		// Player has to be in battle
		if (cm.getState() != CardMaster.STATE_IN_BATTLE)
			return;

		Board board = cm.getCurrentBoard();

		// Board has to be in the placement phase
		if (board.getState() != Board.STATE_WAIT_FOR_PLACEMENT)
			return;

		board.playerFinishedPlacement(cm);
	}
}
