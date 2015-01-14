package core.handlers;

import core.board.ServerBoard;
import core.board.TurnManager;
import core.main.*;
import shared.board.Board;
import shared.items.types.CardItem;
import program.main.Program;
import shared.map.CardMaster;
import shared.other.DataUtil;

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

		ServerCardMaster cm = gameClient.getCardMaster();
		ServerBoard board = cm.getCurrentBoard();

		int id = DataUtil.byteToInt(data);
		CardItem pickedCard = cm.getInventory().findById(id, CardItem.class);

		if (pickedCard == null)
			return;

		TurnManager.getInstance().pick(board, cm, id);
	}

	public void handle(LocalClient localClient, Packet data) {}
}
