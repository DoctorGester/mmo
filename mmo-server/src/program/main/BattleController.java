package program.main;

import shared.board.Alliance;
import core.board.ServerBoard;
import shared.board.Board;
import shared.board.BoardSetup;
import core.board.ai.AI;
import core.board.ai.AIManager;
import core.main.*;
import program.main.database.entities.MatchEntity;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.List;

/**
 * @author doc
 */
public class BattleController {

	protected final List<Board> boards = new LinkedList<Board>();

	private Program program;

	private float tpf;
	private float previousTime;
	private long startTime;

	private static final long TIMER_RESOLUTION = 1000000000L;

	public BattleController(){
		program = Program.getInstance();

		startTime = System.nanoTime();
	}

	private MatchEntity createMatchEntity(BoardSetup setup, ServerCardMaster... cardMasters) {
		int idArray[] = new int[cardMasters.length];

		for (int i = 0; i < cardMasters.length; i++)
			idArray[i] = cardMasters[i].getId();

		try {
			MatchEntity match = new MatchEntity();

			match.setSetup(setup.toBytes());
			match.setParticipants(DataUtil.intToVarInt(idArray));

			program.getDatabase().getMatchDao().create(match);

			return match;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private Board createBattleBoard(BoardSetup setup, CardMaster... cardMasters){
		MatchEntity match = createMatchEntity(setup);

		Board board = new ServerBoard(setup.getWidth(), setup.getHeight());
		board.setId(match.getId());
		board.setPlacementArea(setup.getPlacementAreas());

		for(CardMaster cm: cardMasters){
			cm.setState(CardMaster.STATE_IN_BATTLE);
			cm.setCurrentBoard(board);
			board.addCardMaster(cm);
		}

		int index = 0;
		for (Integer[] alliance: setup.getAlliances()){
			Alliance toAdd = new Alliance();
			toAdd.setId(index++);
			for (int id: alliance)
				toAdd.addCardMaster(board.getCardMasters().get(id));
			board.addAlliance(toAdd);
		}

		board.setTurnTime(setup.getTurnTime());

		board.selectTurningPlayer();

		synchronized (boards){
			boards.add(board);
		}

		return board;
	}

	private Packet createBattlePacket(Board board, BoardSetup boardSetup, CardMaster... masters){
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream(bytes);

			stream.writeInt(board.getId());

			stream.writeByte(masters.length);
			for(CardMaster cardMaster: masters)
				stream.writeInt(cardMaster.getId());

			stream.write(boardSetup.toBytes());

			Packet battlePacket = Packet.getPool().getObj();
			battlePacket.setData(Program.HEADER_BATTLE_BEGINS, bytes.toByteArray());

			return battlePacket;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void startBattle(BoardSetup setup, ServerCardMaster... masters){
		// Shuffling list if required
		if (setup.isShuffle()){
			List<ServerCardMaster> list = Arrays.asList(masters);
			Collections.shuffle(list);
			masters = list.toArray(new ServerCardMaster[masters.length]);
		}

		Board board = createBattleBoard(setup, masters);

		List<GameClient> toSend = new LinkedList<GameClient>();
		List<ServerCardMaster> aiTargets = new LinkedList<ServerCardMaster>();

		for (ServerCardMaster cardMaster: masters){
			GameClient gameClient = program.cardMasterGameClientMap.get(cardMaster);

			if (gameClient != null)
				toSend.add(gameClient);

			if (program.cardMasterNpcMap.containsKey(cardMaster))
				aiTargets.add(cardMaster);
		}


		Packet battlePacket = createBattlePacket(board, setup, masters);

		for(GameClient gameClient: toSend){
			// Cancelling duels
			program.getDuelController().cancel(gameClient.getCardMaster());

			ReliablePacketManager.sendPacket(program.localServer, gameClient.getClient(), battlePacket);
		}

		for(ServerCardMaster master: aiTargets){
			AI ai = new AI(master);
			AIManager.getInstance().addAI(ai);
		}

		Packet.getPool().returnObj(battlePacket);
	}

	public void checkIfClientIsGoingToBattle(GameClient client){
		ServerCardMaster cardMaster = client.getCardMaster();
		List<ServerCardMaster> cardMasters = program.clusterGrid.getHeroesInRadiusOf(cardMaster, 50f);
		for (ServerCardMaster closeOne: cardMasters){
			if (closeOne != cardMaster
					&& closeOne.getState() == CardMaster.STATE_IN_GLOBAL_MAP
					&& program.getNpcByCardMaster(closeOne) != null){
				BoardSetup setup = new BoardSetup()
						.setWidth(8)
						.setHeight(8)
						.addPlacementArea(new Rectangle(1, 0, 5, 2))
						.addPlacementArea(new Rectangle(1, 6, 5, 2))
						.addAlliance(new Integer[] { 0 })
						.addAlliance(new Integer[] { 1 })
						.setShuffle(true)
						.setTurnTime(20f);

				startBattle(setup, cardMaster, closeOne);
				return;
			}
		}
	}

	private void destroyBoard(Board board){
		for (CardMaster cardMaster: board.getCardMasters()){
			cardMaster.setState(CardMaster.STATE_IN_GLOBAL_MAP);
			if (AIManager.getInstance().removeAI(cardMaster)){
				cardMaster.setState(CardMaster.STATE_IN_BATTLE);
			}
		}
	}

	private void computeTimePerFrame(){
		tpf = (System.nanoTime() - startTime - previousTime) * (1.0f / TIMER_RESOLUTION);
		previousTime = System.nanoTime() - startTime;
	}

	public void update(){
		computeTimePerFrame();
		synchronized (boards){
			for(Iterator<Board> iterator = boards.iterator(); iterator.hasNext(); ){
				Board board = iterator.next();
				board.update(tpf);
				if (board.getState() == Board.STATE_GAME_IS_OVER){
					iterator.remove();
					destroyBoard(board);
				}
			}
		}
	}
}
