package core.handlers;

import core.board.BoardSetup;
import core.main.*;
import program.main.Program;

import java.awt.*;

public class AttackPlayerMessageHandler extends PacketHandler{
	private static final float ATTACK_RANGE = 300f;

	private Program program;

	public AttackPlayerMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		// Exit if packet contains other data than id
		if (data.getData().length != 4)
			return;

		int id = DataUtil.byteToInt(data.getData());

		GameClient sender = program.findClient(client);

		BoardSetup setup = new BoardSetup()
								.setWidth(8)
								.setHeight(8)
								.addPlacementArea(new Rectangle(1, 0, 5, 2))
								.addPlacementArea(new Rectangle(1, 6, 5, 2))
								.addAlliance(new Integer[] { 0 })
								.addAlliance(new Integer[] { 1 })
								.setShuffle(true)
								.setTurnTime(60f);

		CardMaster first = sender.getCardMaster();
		CardMaster second = program.getCardMasterById(id);

		if (first.getState() != CardMaster.STATE_IN_GLOBAL_MAP || second.getState() != CardMaster.STATE_IN_GLOBAL_MAP)
			return;

		Vector2f firstPos = new Vector2f(first.getHero().getX(), first.getHero().getY());
		Vector2f secondPos = new Vector2f(second.getHero().getX(), second.getHero().getY());
		float dist = firstPos.distance(secondPos);

		if (dist <= ATTACK_RANGE)
			program.getBattleController().startBattle(setup, first, second);
	}

	public void handle(LocalClient localClient, Packet data) {}
}
