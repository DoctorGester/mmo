package core.handlers;

import core.board.ClientBoard;
import core.board.turns.TurnPick;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import program.datastore.*;
import program.main.Program;
import program.main.SceneUtil;
import shared.items.types.CardItem;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class BattlePickMessageHandler extends PacketHandler {
	private Program program;

	public BattlePickMessageHandler(byte header[]) {
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
		int id = stream.readByte();

		final ClientBoard board = program.getBattleController().getBattleState(boardNumber).getBoard();;
		final CardMaster cardMaster = board.getCardMasters().get(id);

		int cardId = stream.readInt();
		final CardItem pickedCard = ItemDatabase.getInstance().getOrCreateItem(cardId, CardItem.class);

		ItemDatabase.getInstance().subscribe(cardId, new Subscriber() {
			@Override
			public void receive(String key, Data subscription) {
				TurnPick turn = new TurnPick(board, cardMaster, pickedCard);

				SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).getTurnQueue().add(turnNumber, turn);
			}
		});
		ItemDatabase.getInstance().requestItem(pickedCard);
	}

}
