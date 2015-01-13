package core.handlers;

import core.board.TurnManager;
import core.board.ai.AIManager;
import core.board.interfaces.Board;
import core.board.interfaces.Cell;
import core.main.*;
import core.main.inventory.items.CardItem;
import program.main.Program;

public class BattlePickMessageHandler extends PacketHandler {
	private Program program;

	public BattlePickMessageHandler(byte header[]) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		GameClient gameClient = program.findClient(client);

		// Exit if client is missing or not in the battle state
		if (gameClient == null || gameClient.getCardMaster().getState() != CardMaster.STATE_IN_BATTLE)
			return;

		byte data[] = packet.getData();

		// Exit if no data provided at all
		if (data.length != 4)
			return;

		CardMaster cm = gameClient.getCardMaster();
		Board board = cm.getCurrentBoard();

		int id = DataUtil.byteToInt(data);
		CardItem pickedCard = cm.getInventory().findById(id, CardItem.class);

		if (pickedCard == null)
			return;

		TurnManager.getInstance().pick(board, cm, id);
	}

	public void handle(LocalClient localClient, Packet data) {}
}
