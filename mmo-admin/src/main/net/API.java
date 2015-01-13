package main.net;

import com.google.gson.Gson;
import core.main.LocalClient;
import core.main.Packet;
import main.models.FindPlayerModel;
import main.net.handlers.CheckCredentialsHandler;
import main.net.handlers.GetPlayerInfoHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.Charset;

/**
 * @author doc
 */
public class API {
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	public static final byte[] HEADER_ADMIN = new byte[] { 127, 126 },
							   HEADER_ADMIN_GET_PLAYER_INFO = new byte [] { 0, 33 },
							   HEADER_ADMIN_CHECK_CREDENTIALS = new byte[] { 0, 34 };

	private LocalClient client;
	private final String login;
	private final String password;

	public API(LocalClient connectedClient, String login, String password){
		client = connectedClient;
		this.login = login;
		this.password = password;

		client.addPacketHandler(new GetPlayerInfoHandler(HEADER_ADMIN_GET_PLAYER_INFO));
		client.addPacketHandler(new CheckCredentialsHandler(HEADER_ADMIN_CHECK_CREDENTIALS));
	}

	public void requestPlayerInfo(int playerId){
		sendRequest(HEADER_ADMIN_GET_PLAYER_INFO, new FindPlayerModel(playerId, null));
	}

	public void requestPlayerInfo(String name){
		sendRequest(HEADER_ADMIN_GET_PLAYER_INFO, new FindPlayerModel(0, name));
	}

	public void checkCredentials(){
		sendRequest(HEADER_ADMIN_CHECK_CREDENTIALS, null);
	}

	private void sendRequest(byte[] header, Object request){
		Gson gson = new Gson();

		String string = (request != null) ? gson.toJson(request) : "{}";

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeUTF(login);
			stream.writeUTF(password);
			stream.write(header);
			stream.write(string.getBytes(UTF_8));

			client.send(new Packet(HEADER_ADMIN, bytes.toByteArray()));

			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
