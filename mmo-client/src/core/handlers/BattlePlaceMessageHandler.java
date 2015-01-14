package core.handlers;

import core.board.turns.Turn;
import core.board.turns.TurnPlace;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import program.datastore.DataKey;
import program.datastore.DataStore;
import program.datastore.ExistenceCondition;
import program.datastore.GameStateCondition;
import program.main.Program;
import program.main.SceneUtil;
import shared.board.Board;
import shared.board.Cell;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class BattlePlaceMessageHandler extends PacketHandler {
	private Program program;

	public BattlePlaceMessageHandler(byte header[]) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
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
		}, new ExistenceCondition(DataKey.MAIN_PLAYER), new ExistenceCondition(DataKey.INVENTORY), new GameStateCondition(Program.STATE_BATTLE));
	}

	private void delegate(byte data[]) throws IOException {
		DataInputStream stream = DataUtil.stream(data);

		int boardNumber = stream.readInt();
		short turnNumber = stream.readShort();

		Board board = program.getBattleController().getBattleState(boardNumber).getBoard();

		Turn turn;

		if (stream.available() == 0){
			turn = new TurnPlace();
		} else {
			Cell selected = board.getCellChecked(stream.readByte(), stream.readByte()),
				   target = board.getCellChecked(stream.readByte(), stream.readByte());

			turn = new TurnPlace(selected, target);
		}

		SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);

	}

}
