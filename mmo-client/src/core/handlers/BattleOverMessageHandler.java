package core.handlers;

import core.board.turns.Turn;
import core.board.turns.TurnBattleOver;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import core.ui.BattleState;
import core.ui.battle.BattleOverUIState;
import program.datastore.DataStore;
import program.datastore.GameStateCondition;
import program.main.Program;
import program.main.SceneUtil;
import shared.board.Board;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class BattleOverMessageHandler extends PacketHandler{
	private Program program;

	public BattleOverMessageHandler(byte header[]){
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
		byte battleResult = stream.readByte();

		BattleState battleState = program.getBattleController().getBattleState(boardNumber);
		Board board = battleState.getBoard();

		CardMaster winners[] = new CardMaster[0];
		int status = BattleOverUIState.STATUS_DRAW;

		if (battleResult == Board.GAME_OVER_WIN){
			List<CardMaster> alliance = board.getAllianceById(stream.readByte()).getAlliance();
			winners = alliance.toArray(new CardMaster[alliance.size()]);
			status = BattleOverUIState.STATUS_DEFEAT;
			if (alliance.contains(Program.getInstance().getMainPlayer()))
				status = BattleOverUIState.STATUS_VICTORY;
		}

		Turn turn = new TurnBattleOver(battleState, status, winners);
		SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
	}


}
