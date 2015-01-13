package core.handlers;

import core.board.Board;
import core.board.turns.Turn;
import core.board.turns.TurnCastCardSpell;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import core.main.inventory.items.SpellCardItem;
import core.ui.UI;
import core.ui.battle.SpellSelectorUIState;
import program.datastore.DataKey;
import program.datastore.DataStore;
import program.datastore.ExistenceCondition;
import program.datastore.GameStateCondition;
import program.main.Program;
import program.main.Util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CastCardSpellMessageHandler extends PacketHandler {
	private Program program;

	public CastCardSpellMessageHandler(byte header[]) {
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
		CardMaster cardMaster = board.getCardMasters().get(stream.readByte());

		final SpellCardItem item = new SpellCardItem();
		item.setId(stream.readInt());

		if (cardMaster == Program.getInstance().getMainPlayer()){

			Program.getInstance().getMainFrame().enqueue(new Callable<Object>() {
				public Object call() throws Exception {
					Util.getUI(UI.STATE_SPELL_SELECTOR, SpellSelectorUIState.class).removeCard(item);
					return null;
				}
			});
		}

		Turn turn = new TurnCastCardSpell(board, cardMaster, item);
		Util.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
	}

}
