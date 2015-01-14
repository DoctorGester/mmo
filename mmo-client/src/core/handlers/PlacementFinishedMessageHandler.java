package core.handlers;

import core.board.turns.Turn;
import core.board.turns.TurnFinishPlacement;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import core.ui.BattleState;
import program.datastore.DataStore;
import program.datastore.GameStateCondition;
import program.main.Program;
import program.main.SceneUtil;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class PlacementFinishedMessageHandler extends PacketHandler {
	private Program program;

	public PlacementFinishedMessageHandler(byte[] header) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalClient localClient, Packet packet) {
		final byte data[] = packet.getData();
		DataStore.getInstance().awaitAndExecute(new Runnable() {
			@Override
			public void run() {
				try {
					delegate(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, new GameStateCondition(Program.STATE_BATTLE));
	}

	private void delegate(byte data[]) throws IOException {
		DataInputStream stream = DataUtil.stream(data);

		int boardNumber = stream.readInt();
		short turnNumber = stream.readShort();

		BattleState battleState = program.getBattleController().getBattleState(boardNumber);

		byte dataLeft[] = new byte[stream.available()];
		stream.readFully(dataLeft);

		Turn turn = new TurnFinishPlacement(battleState, dataLeft);
		SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}
}
