package core.handlers;

import core.board.Board;
import core.board.Cell;
import core.board.turns.Turn;
import core.board.turns.TurnCast;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import program.datastore.DataStore;
import program.datastore.GameStateCondition;
import program.main.ConditionalAction;
import program.main.Program;
import program.main.Util;

import java.io.DataInputStream;
import java.io.IOException;

public class CastSpellMessageHandler extends PacketHandler {
	private Program program;

	public CastSpellMessageHandler(byte header[]) {
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

		Board board = program.getBattleController().getBattleState(boardNumber).getBoard();

		Cell selected = board.getCellChecked(stream.readByte(), stream.readByte()),
			   target = board.getCellChecked(stream.readByte(), stream.readByte());

		Turn turn = new TurnCast(board, selected, target, spell);
		Util.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
	}

}
