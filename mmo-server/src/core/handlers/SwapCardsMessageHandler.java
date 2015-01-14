package core.handlers;

import core.main.*;
import program.main.Program;
import shared.map.CardMaster;
import shared.other.DataUtil;

@Deprecated
public class SwapCardsMessageHandler extends PacketHandler {
	private Program program;

	public SwapCardsMessageHandler(byte header[]) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		GameClient gameClient = program.findClient(client);

		// Exit if client is missing or not in the global map state
		if (gameClient == null)
			return;

		if (gameClient.getCardMaster().getState() != CardMaster.STATE_IN_GLOBAL_MAP)
			return;

		byte data[] = packet.getData();

		// Exit if no data length is wrong
		if (data.length != 4)
			return;

		CardMaster cardMaster = gameClient.getCardMaster();

		short swapTargetOne = DataUtil.byteToShort(new byte[]{data[0], data[1]}),
			  swapTargetTwo = DataUtil.byteToShort(new byte[]{ data[2], data[3] });

		// Exit if swap targets are wrong
		if (swapTargetOne == swapTargetTwo || swapTargetOne < 0 || swapTargetTwo < 0)
			return;

		/*List<Card> cards = cardMaster.getCards();

		if (swapTargetOne >= cards.size() || swapTargetTwo >= cards.size())
			return;

		// Swap cards and send updated deck back
		Collections.swap(cardMaster.getCards(), swapTargetOne, swapTargetTwo);
		gameClient.updateDeckRaw();
		ReliablePacketManager.sendPacket(localServer, client, Program.HEADER_GET_INVENTORY, gameClient.getDeckRaw());*/
	}

	public void handle(LocalClient localClient, Packet data) {
	}

}
