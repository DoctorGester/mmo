package core.main;

import program.main.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class ChatChannel {
	private int id;
	private String name = "Undefined";
	private List<GameClient> clients = new ArrayList<GameClient>();

	public ChatChannel(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void addClient(GameClient client){
		clients.add(client);
	}

	public void removeClient(GameClient client){
		clients.remove(client);
	}

	public List<GameClient> getClients() {
		return clients;
	}

	public void sendAll(byte header[], byte data[]){
		try {
			byte idBytes[] = DataUtil.intToByte(id);
			byte sendData[] = new byte[idBytes.length + data.length];
			System.arraycopy(idBytes, 0, sendData, 0, idBytes.length);
			System.arraycopy(data, 0, sendData, idBytes.length, data.length);

			Packet packet = Packet.getPool().getObj();
			packet.setData(header, sendData);
			sendAll(packet);
			Packet.getPool().returnObj(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendAll(Packet packet){
		LocalServer server = Program.getInstance().getLocalServer();

		for(GameClient client: clients)
			server.send(client.getClient(), packet);
	}

	public String getName() {
		return name;
	}
}
