package core.handlers;

import core.main.*;
import program.main.Program;
import program.main.ReliablePacketManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class GetPositionInfoMessageHandler extends PacketHandler{

	private Program program;

	public GetPositionInfoMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

    private static final int MAXIMUM_DATA_PIECE_LENGTH = 16;

	public void handle(LocalServer localServer, Client client, Packet data) {
		GameClient sender = program.findClient(client);
		
		if (sender == null)
			return;
		
		int idArray[] = DataUtil.varIntsToInts(data.getData());

		if (idArray.length == 0)
			return;

		if (idArray.length > MAXIMUM_DATA_PIECE_LENGTH)
			idArray = Arrays.copyOf(idArray, MAXIMUM_DATA_PIECE_LENGTH);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		Set<CardMaster> request = new HashSet<CardMaster>();
		for (int id: idArray) {
			CardMaster master = program.getCardMasterById(id);

			if (master != null)
				request.add(master);
		}

		stream.write(request.size());

		try {
			for (CardMaster master: request) {
				stream.write(DataUtil.intToByte(master.getId()));
				stream.write(master.getPositionInfo());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ReliablePacketManager.sendPacket(localServer, client, Program.HEADER_GET_POSITION_INFO, stream.toByteArray());
	}

	public void handle(LocalClient localClient, Packet data) {}
}