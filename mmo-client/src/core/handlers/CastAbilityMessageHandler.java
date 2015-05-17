package core.handlers;

import core.board.ClientBoard;
import core.board.ClientCell;
import core.board.turns.Turn;
import core.board.turns.TurnCast;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import program.datastore.DataStore;
import program.datastore.GameStateCondition;
import program.main.Program;
import program.main.SceneUtil;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class CastAbilityMessageHandler extends PacketHandler {
	private Program program;

	public CastAbilityMessageHandler(byte header[]) {
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
		}, new GameStateCondition(Program.STATE_BATTLE));
	}

	private void delegate(byte data[]) throws IOException {
		DataInputStream stream = DataUtil.stream(data);

		int boardNumber = stream.readInt();
		short turnNumber = stream.readShort();

		int spell = stream.readByte();

		ClientBoard board = program.getBattleController().getBattleState(boardNumber).getBoard();

		ClientCell selected = board.getCellChecked(stream.readByte(), stream.readByte()),
			   	   target = board.getCellChecked(stream.readByte(), stream.readByte());

		Turn turn = new TurnCast(board, selected, target, spell);
		SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
	}

}
