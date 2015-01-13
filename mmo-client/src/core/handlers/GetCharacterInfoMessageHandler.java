package core.handlers;

import core.main.*;
import program.main.Program;

import java.io.DataInputStream;
import java.io.IOException;

public class GetCharacterInfoMessageHandler extends PacketHandler{
	public GetCharacterInfoMessageHandler(byte header[]){
		super(header);
	}

	public void handle(LocalClient localClient, Packet data) {
		DataInputStream stream = DataUtil.stream(data.getData());

		try {
			int amount = stream.readByte();

			for (int i = 0; i < amount; i++){
				int id = stream.readInt();

				CardMaster master = Program.getInstance().getVisiblePlayerById(id);

				if (master != null){
					master.setCharacterInfo(stream);
					master.setInitialized(true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}
}