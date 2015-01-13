package core.handlers;

import core.board.interfaces.Board;
import core.main.*;
import program.main.Program;

public class SkipTurnMessageHandler extends PacketHandler{
	private Program program;

	public SkipTurnMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		// Exit if packet contains other data than header
		if (data.getData().length != 0)
			return;
		
		GameClient gc = program.findClient(client);
		
		// Exit if client does not exist or is not in battle state
		if (gc == null)
			return;

		if (gc.getCardMaster().getState() != CardMaster.STATE_IN_BATTLE)
			return;

		CardMaster cm = gc.getCardMaster();
		Board board = cm.getCurrentBoard();

		if ((board.getState() == Board.STATE_WAIT_FOR_ORDER || board.getState() == Board.STATE_WAIT_FOR_PICK)
				&& board.getCurrentTurningPlayer() == cm)
			board.setTimeRemaining(0.5f);
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
