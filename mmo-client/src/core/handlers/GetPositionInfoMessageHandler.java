package core.handlers;

import core.main.*;
import program.main.Program;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;

public class GetPositionInfoMessageHandler extends PacketHandler{
	public GetPositionInfoMessageHandler(byte header[]){
		super(header);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}

	public void handle(LocalClient localClient, Packet data) {
		DataInputStream stream = DataUtil.stream(data.getData());

		try {
			int amount = stream.readByte();

			for (int i = 0; i < amount; i++){
				int id = stream.readInt();

				ClientCardMaster master = Program.getInstance().getVisiblePlayerById(id);

				if (master != null)
					master.setPositionInfo(stream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}