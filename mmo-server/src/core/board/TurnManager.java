package core.board;

import shared.board.Alliance;
import shared.board.Board;
import shared.board.Cell;
import shared.board.Unit;
import shared.map.CardMaster;
import shared.other.DataUtil;
import core.main.GameClient;
import shared.items.types.CardItem;
import shared.items.types.SpellCardItem;
import program.main.Program;
import program.main.ReliablePacketManager;

import java.util.List;

/**
 * @author doc
 */
public class TurnManager {
	private static TurnManager instance = new TurnManager();

	public static TurnManager getInstance() {
		return instance;
	}

	private TurnManager() {
	}

	public void time(Board board, float time){
		sendAll(board, Program.HEADER_BATTLE_TIMER_UPDATE, DataUtil.floatToByte(time));
	}

	public void gameOver(Board board, int state, Alliance winner){
		byte data[] = null;

		if (state == Board.GAME_OVER_WIN){
			data = new byte[]{
				(byte) state,
				(byte) winner.getId()
			};
		} else if (state == Board.GAME_OVER_DRAW){
			data = new byte[]{
					(byte) state
			};
		}
		sendAll(board, Program.HEADER_BATTLE_IS_OVER, data);
	}

	public boolean smart(ServerBoard board, CardMaster owner, Cell from, Cell to){
		if(board.handleSimpleOrder(owner, from, to)) {
			byte data[] = new byte[]{
				(byte) from.getX(),
				(byte) from.getY(),
				(byte) to.getX(),
				(byte) to.getY()
			};

			sendAll(board, Program.HEADER_BATTLE_MOVE_ORDER, data);

			board.checkGameOver();
			return true;
		}
		return false;
	}

	public boolean pick(ServerBoard board, CardMaster owner, int cardItemId){
		CardItem card = owner.getInventory().findById(cardItemId, CardItem.class);
		Unit picked = board.handlePickOrder(owner, card, Program.getInstance().getUnitDataById(card.getUnitId()));

		if (picked != null){
			byte data[] = new byte[]{
					(byte) owner.getBattleId(),
			};

			data = DataUtil.concatenate(data, DataUtil.intToByte(card.getId()));

			sendAll(board, Program.HEADER_BATTLE_PICK_ORDER, data);
			return true;
		}
		return false;
	}

	public boolean place(ServerBoard board, CardMaster owner, Cell from, Cell to){
		if (board.handlePlacementOrder(owner, from, to)){
			Program program = Program.getInstance();

			final byte senderData[] = new byte[]{
				(byte) from.getX(),
				(byte) from.getY(),
				(byte) to.getX(),
				(byte) to.getY()
			};

			final GameClient sender = program.getGameClientByCardMaster(owner);

			sendAll(board, Program.HEADER_BATTLE_PLACE_ORDER, new PlayerDataFilter() {
				@Override
				public byte[] getDataForPlayer(GameClient player) {
					return (player == sender) ? senderData : new byte[0];
				}
			});
			return true;
		}
		return false;
	}

	public boolean cast(ServerBoard board, CardMaster owner, int spell, Cell from, Cell to){
		if (board.handleCastOrder(owner, from, to, spell)){
			byte data[] = new byte[]{
				(byte) spell,
				(byte) from.getX(),
				(byte) from.getY(),
				(byte) to.getX(),
				(byte) to.getY()
			};

			sendAll(board, Program.HEADER_SPELL_CAST_ORDER, data);

			board.checkGameOver();
			return true;
		}
		return false;
	}

	public boolean castCard(ServerBoard board, CardMaster caster, int cardId){
		if (board.handleCastCardSpellOrder(caster, cardId)){
			SpellCardItem spellCard = caster.getInventory().findById(cardId, SpellCardItem.class);

			byte data[] = DataUtil.concatenate(new byte[]{ (byte) caster.getBattleId() }, DataUtil.intToByte(spellCard.getId()));

			sendAll(board, Program.HEADER_CARD_CAST_ORDER, data);

			board.checkGameOver();
			return true;
		}
		return false;
	}

	public boolean skip(ServerBoard board, CardMaster owner){
		// Skip turn if board is in idle state and turning player is right
		if (board.getState() == Board.STATE_WAIT_FOR_ORDER && board.getCurrentTurningPlayer() == owner){
			board.skipTurn();
			sendAll(board, Program.HEADER_BATTLE_SKIP_TURN, new byte[0]);

			board.checkGameOver();
			return true;
		}
		return false;
	}

	public void finishPlacement(Board board){
		// If board state has changed to idle state, send all unit information to clients
		if (board.getState() == Board.STATE_WAIT_FOR_ORDER){
			List<? extends Unit> units = board.getUnits();
			byte data[] = new byte[units.size() * 2];
			int number = 0;
			for(Unit unit: units){
				data[number] = (byte) unit.getPosition().getX();
				data[number + 1] = (byte) unit.getPosition().getY();
				number += 2;
			}

			sendAll(board, Program.HEADER_PLACEMENT_FINISHED, data);
		}
	}

	private void sendAll(Board board, byte header[], final byte data[]){
		sendAll(board, header, new PlayerDataFilter() {
			@Override
			public byte[] getDataForPlayer(GameClient player) {
				return data;
			}
		});
	}

	private void sendAll(Board board, byte header[], PlayerDataFilter filter){
		byte[] boardId = DataUtil.intToByte(board.getId());
		byte[] turnId = DataUtil.shortToByte(board.getTurnNumber());

		Program program = Program.getInstance();
		for(CardMaster cardMaster: board.getCardMasters()){
			GameClient player = program.getGameClientByCardMaster(cardMaster);
			if (player == null)
				continue;

			// Adding boardId and turnId to data
			byte data[] = filter.getDataForPlayer(player);

			data = DataUtil.concatenate(turnId, data);
			data = DataUtil.concatenate(boardId, data);

			ReliablePacketManager.sendPacket(program.getLocalServer(), player.getClient(), header, data);
		}
	}

	private static interface PlayerDataFilter {
		public byte[] getDataForPlayer(GameClient player);
	}
}