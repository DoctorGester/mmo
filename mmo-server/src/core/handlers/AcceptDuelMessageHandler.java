package core.handlers;

import core.main.*;
import program.main.Program;

public class AcceptDuelMessageHandler extends PacketHandler{
	private Program program;

	public AcceptDuelMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		// Exit if packet contains other data than id
		if (data.getData().length != 4)
			return;

		int id = DataUtil.byteToInt(data.getData());

		GameClient sender = program.findClient(client);
		GameClient target = program.getGameClientByCardMaster(program.getCardMasterById(id));

		if (target == null)
			return;

		CardMaster first = sender.getCardMaster();
		CardMaster second = target.getCardMaster();

		if (first.getState() != CardMaster.STATE_IN_GLOBAL_MAP || second.getState() != CardMaster.STATE_IN_GLOBAL_MAP)
			return;

		if (sender.getPlayersInSight().contains(second))
			program.getDuelController().accept(second);
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
