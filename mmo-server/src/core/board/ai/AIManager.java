package core.board.ai;

import core.main.ServerCardMaster;
import shared.board.Board;
import shared.map.CardMaster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AIManager {
	private static final AIManager instance = new AIManager();

	public static AIManager getInstance(){
		return instance;
	}

	private final Map<CardMaster, AI> cardMasterAIMap;

	private AIManager(){
		cardMasterAIMap = new HashMap<CardMaster, AI>();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new UpdateAITask(), 1, 1, TimeUnit.SECONDS);
	}

	public void addAI(AI ai){
		synchronized (cardMasterAIMap){
			cardMasterAIMap.put(ai.cardMaster, ai);
			ai.setBoard(ai.cardMaster.getCurrentBoard());
		}
	}

	public boolean removeAI(CardMaster cardMaster){
		synchronized (cardMasterAIMap){
			AI ai = cardMasterAIMap.remove(cardMaster);
			return ai != null;
		}
	}

	private class UpdateAITask implements Runnable {

		public void run(){
			synchronized (cardMasterAIMap){
				for (Map.Entry<CardMaster, AI> entry: cardMasterAIMap.entrySet()){
					AI ai = entry.getValue();
					Board board = ai.realBoard;
					int state = board.getState();

					if (state == Board.STATE_WAIT_FOR_PLACEMENT)
						ai.doPlacement();

					if (board.getCurrentTurningPlayer() != entry.getKey())
						continue;

					if (state == Board.STATE_WAIT_FOR_PICK)
						ai.pickCard();

					if (state == Board.STATE_WAIT_FOR_ORDER)
						ai.makeTurn();
				}
			}
		}
	}
}
