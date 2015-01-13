package main.net.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import core.main.*;
import main.core.Program;
import main.models.PlayerInfo;
import main.net.API;

/**
 * @author doc
 */
public class GetPlayerInfoHandler extends PacketHandler {
	public GetPlayerInfoHandler(byte[] header) {
		super(header);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}

	public void handle(LocalClient localClient, Packet data) {
		String raw = new String(data.getData(), API.UTF_8);
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		PlayerInfo info = gson.fromJson(raw, PlayerInfo.class);

		Program.getInstance().getFrame().getPlayerForm().fillPlayerInfo(info);
	}
}
