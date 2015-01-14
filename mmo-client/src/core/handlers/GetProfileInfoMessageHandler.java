package core.handlers;

import core.main.*;
import core.ui.map.ProfileInfo;
import program.datastore.DataStore;
import program.main.Program;
import shared.map.Stat;
import shared.other.DataUtil;

import java.io.DataInputStream;

public class GetProfileInfoMessageHandler extends PacketHandler{
	private Program program;

	public GetProfileInfoMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalClient localClient, final Packet data) {
		try {
			DataInputStream stream = DataUtil.stream(data.getData());

			ProfileInfo info = new ProfileInfo();

			info.setId(stream.readInt());
			info.setName(stream.readUTF());

			int factionAmount = stream.readByte();

			for (int i = 0; i < factionAmount; i++){
				int factionId = stream.readByte();
				int reputation = stream.readByte();

				info.setReputation(program.getFactionById(factionId), reputation);
			}

			for (Stat stat: Stat.values())
				info.setStat(stat, stream.readByte());

			DataStore.getInstance().put(ProfileInfo.getKey(info.getId()), info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}
}
