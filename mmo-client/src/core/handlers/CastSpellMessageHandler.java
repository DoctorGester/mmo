package core.handlers;

import core.board.ClientBoard;
import core.board.turns.Turn;
import core.board.turns.TurnCastSpell;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import core.ui.UI;
import core.ui.battle.SpellSelectorUIState;
import program.datastore.*;
import program.main.Program;
import program.main.SceneUtil;
import shared.items.types.SpellCardItem;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

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
		}, new ExistenceCondition(DataKey.MAIN_PLAYER), new ExistenceCondition(DataKey.INVENTORY), new GameStateCondition(Program.STATE_BATTLE));
	}

	private void delegate(byte data[]) throws IOException {
		DataInputStream stream = DataUtil.stream(data);

		int boardNumber = stream.readInt();
		final short turnNumber = stream.readShort();

		final ClientBoard board = program.getBattleController().getBattleState(boardNumber).getBoard();
		final CardMaster cardMaster = board.getCardMasters().get(stream.readByte());

		final SpellCardItem item = ItemDatabase.getInstance().getOrCreateItem(stream.readInt(), SpellCardItem.class);

		if (cardMaster == Program.getInstance().getMainPlayer()){

			Program.getInstance().getMainFrame().enqueue(new Callable<Object>() {
				public Object call() throws Exception {
					SceneUtil.getUI(UI.STATE_SPELL_SELECTOR, SpellSelectorUIState.class).removeCard(item);
					return null;
				}
			});
		}

		ItemDatabase.getInstance().requestItem(item, new Subscriber() {
			@Override
			public void receive(String key, Data subscription) {
				Turn turn = new TurnCastSpell(board, cardMaster, item);
				SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
			}
		});
	}

}
