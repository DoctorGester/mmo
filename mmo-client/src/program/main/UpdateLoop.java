package program.main;

import core.exceptions.IncorrectHeaderException;
import core.exceptions.IncorrectPacketException;
import core.main.*;
import core.ui.BattleState;
import program.datastore.DataKey;
import program.datastore.DataStore;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

public class UpdateLoop implements Runnable {
	private final Program program;
	protected final Queue<Callable> queue = new LinkedBlockingQueue<Callable>();

	private int onlineUpdateTick, positionInfoUpdateTick;
	private Packet onlineUpdatePacket = null;

	private float tpf;
	private float previousTime;
	private long startTime;

	private static final long TIMER_RESOLUTION = 1000000000L;

	public UpdateLoop(Program program) {
		this.program = program;
		try {
			onlineUpdatePacket = new Packet(Program.HEADER_STILL_ONLINE);
		} catch (IncorrectPacketException e1) {
			e1.printStackTrace();
		}

		startTime = System.nanoTime();
	}

	private void tryToDetermineMainClient() throws IncorrectPacketException {
		for (CardMaster player: program.getVisiblePlayers()) {
			if (player.getId() == -1)
				continue;

			if (player.getId() == program.mainId){
				program.mainPlayer = player;
				DataStore.getInstance().put(DataKey.MAIN_PLAYER, program.mainPlayer);

				program.updateInventory();
				break;
			}
		}
	}

	private void computeTimePerFrame(){
		tpf = (System.nanoTime() - startTime - previousTime) * (1.0f / TIMER_RESOLUTION);
		previousTime = System.nanoTime() - startTime;
	}

	public void run() {
		// Simply exit, if application has requested close
		if (program.isOnDestroy())
			System.exit(0);
		try {
			computeTimePerFrame();

			synchronized (queue){
				if (!queue.isEmpty())
					queue.poll().call();
			}

			// Try to determine main client if it's null
			if (program.mainPlayer == null)
				tryToDetermineMainClient();
			updateGlobalMap();

			for (BattleState state: program.getBattleController().getBattleStates())
				state.getBoard().update(tpf);

			// Sending online update packet every 2 s
			if (program.getLocalClient() != null && ++onlineUpdateTick >= 100) {
				program.getLocalClient().send(onlineUpdatePacket);
				onlineUpdateTick = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateGlobalMap() throws IncorrectHeaderException {
		Set<CardMaster> players = program.getVisiblePlayers();

		// Updating all heroes every 20 ms
		for (CardMaster player: players)
			player.getHero().update();

		// Requesting position info data for visible players every 10 seconds
		if (++positionInfoUpdateTick >= 500) {
			positionInfoUpdateTick = 0;

			int index = 0;
			int idArray[] = new int[players.size()];

			for (CardMaster player: players)
				idArray[index++] = player.getId();

			if (idArray.length > 0)
				program.getLocalClient().send(new Packet(Program.HEADER_GET_POSITION_INFO, DataUtil.intToVarInt(idArray)));
		}
	}
}