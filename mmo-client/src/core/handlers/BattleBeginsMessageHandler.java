package core.handlers;

import core.board.BoardSetup;
import core.main.*;
import core.ui.MapController;
import program.datastore.*;
import program.main.Program;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BattleBeginsMessageHandler extends PacketHandler{
	private Program program;

	public BattleBeginsMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalClient localClient, Packet packet){
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
		}, new ExistenceCondition(DataKey.MAIN_PLAYER), new ActualCondition(DataKey.INVENTORY), new GameStateCondition(Program.STATE_GLOBAL_MAP));

		// Requesting deck update
		program.updateInventory();
	}

	private void delegate(byte data[]) throws IOException{
		DataInputStream stream = DataUtil.stream(data);

		int boardId = stream.readInt();
		int playerAmount = stream.readByte();

		List<CardMaster> cardMasters = new ArrayList<CardMaster>(playerAmount);

		for (int i = 0; i < playerAmount; i++){
			int id = stream.readInt();

			cardMasters.add(program.getOrCreatePlayerById(id));
		}

		BoardSetup boardSetup = new BoardSetup().fromBytes(stream);

		program.getBattleController().initBattle(boardId, boardSetup, cardMasters.toArray(new CardMaster[cardMasters.size()]));
	}


	public void handle(LocalServer localServer, Client client, Packet data) {
	}
}
