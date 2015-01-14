package core.handlers;

import core.main.*;
import program.datastore.DataStore;
import program.datastore.GameStateCondition;
import program.main.Program;
import shared.board.Board;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class BattleTimerUpdateMessageHandler extends PacketHandler{

	public BattleTimerUpdateMessageHandler(byte header[]){
		super(header);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}

	public void handle(LocalClient localClient, Packet data) {
		DataInputStream stream = DataUtil.stream(data.getData());
		try {
			final int battleId = stream.readInt();
			stream.readShort(); // We do not need a turn number in this case

			final float time = stream.readFloat();
			DataStore.getInstance().awaitAndExecute(new Runnable() {
				@Override
				public void run() {
					Board board = Program.getInstance().getBattleController().getBattleState(battleId).getBoard();
					board.setTimeRemaining(time);
				}
			}, new GameStateCondition(Program.STATE_BATTLE));
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
