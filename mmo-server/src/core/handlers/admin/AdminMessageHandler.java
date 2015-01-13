package core.handlers.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import core.exceptions.IncorrectPacketException;
import core.main.*;
import nf.fr.eraasoft.pool.PoolException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminMessageHandler extends PacketHandler {
	public AdminMessageHandler(byte[] header) {
		super(header);
	}

	private static final Gson gson;
	private static final Map<String, String> adminList = new HashMap<String, String>();
	private final Map<PacketHeader, PacketHandler> handlers = new HashMap<PacketHeader, PacketHandler>();

	static {
		adminList.put("root", "root");
		gson = new GsonBuilder()
				//.setPrettyPrinting()
				.enableComplexMapKeySerialization()
				.addSerializationExclusionStrategy(new WhiteListExclusionStrategy()).create();
	}

	public static Gson createGson(){
		return gson;
	}

	public void addPacketHandler(PacketHandler handler){
		try {
			PacketHeader header = PacketHeader.getPool().getObj();
			header.setHeader(handler.getHeader());
			handlers.put(header, handler);
		} catch (PoolException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.getData());
		DataInputStream stream = new DataInputStream(inputStream);

		try {
			String login = stream.readUTF();
			String pass = stream.readUTF();

			byte data[] = new byte[stream.available()];
			stream.readFully(data);

			// Checks corresponding login/pass pair to be present in the admin list
			if (pass.equals(adminList.get(login))) {
				Packet dataPacket = new Packet(data);

				PacketHandler handler = handlers.get(new PacketHeader(dataPacket.getHeader()));

				if (handler != null)
					handler.handle(localServer, client, dataPacket);
			}

			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}

}
