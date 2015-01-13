package core.handlers;

import core.board.turns.Turn;
import core.board.turns.TurnSkip;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import program.main.Program;
import program.main.Util;

import java.io.DataInputStream;
import java.io.IOException;

public class SkipTurnMessageHandler extends PacketHandler{
	private Program program;

	public SkipTurnMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		try {
			DataInputStream stream = DataUtil.stream(data.getData());

			int boardNumber = stream.readInt();
			short turnNumber = stream.readShort();

			Turn turn = new TurnSkip(program.getBattleController().getBattleState(boardNumber).getBoard());
			Util.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
