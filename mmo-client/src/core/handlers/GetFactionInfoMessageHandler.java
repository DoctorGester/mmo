package core.handlers;

import core.main.*;
import program.main.Program;
import shared.other.DataUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class GetFactionInfoMessageHandler extends PacketHandler{
	private Program program;

	public GetFactionInfoMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalClient localClient, Packet data) {
		try {
			DataInputStream stream = DataUtil.stream(data.getData());

			int amount = stream.readByte();

			for (int i = 0; i < amount; i++){
				int id = stream.readByte();
				String name = stream.readUTF();

				Faction faction = program.getFactionById(id);

				if (faction == null){
					faction = new Faction();
					faction.setId(id);
					program.addFaction(faction);
				}

				faction.setName(name);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}
}
