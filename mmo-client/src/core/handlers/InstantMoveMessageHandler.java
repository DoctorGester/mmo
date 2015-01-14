package core.handlers;

import core.main.*;
import program.main.Program;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.util.Arrays;

public class InstantMoveMessageHandler extends PacketHandler {
	private Program program;

	public InstantMoveMessageHandler(byte header[]) {
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		byte b[] = data.getData();

		int offset = 0;
		
		byte bid[] = Arrays.copyOf(b, offset += 4);
		byte bx[] = Arrays.copyOfRange(b, offset, offset += 4);
		byte by[] = Arrays.copyOfRange(b, offset, offset += 4);

		int id = DataUtil.byteToInt(bid);
		float x = DataUtil.byteToFloat(bx);
		float y = DataUtil.byteToFloat(by);

		CardMaster player = program.getVisiblePlayerById(id);

		// Exit if client is not visible or absent
		if (player == null)
			return;

		player.getHero().setX(x);
		player.getHero().setY(y);
	}

}
