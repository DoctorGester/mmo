package core.handlers;

import core.main.*;
import program.main.Program;

import java.util.List;

@Deprecated
public class GoneOutOfSightMessageHandler extends PacketHandler{

	// TODO Remove this class, it is unused
	
	private Program program;
	
	public GoneOutOfSightMessageHandler(byte[] header) {
		super(header);
	}
	
	public GoneOutOfSightMessageHandler(Program program, byte header[]){
		this(header);
		this.program = program;
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		// Just plain array of 4byte ints
		byte b[] = data.getData();
		// Checking if data size is correct
		if (b.length == 0 || b.length % 4 != 0) // int is 4 bytes long
			return;
		int ids[] = new int[b.length / 4];
		byte temp[] = new byte[4]; // temp array for holding int data
		
		for(int i = 0; i < b.length; i += 4){
			System.arraycopy(b, i, temp, 0, 4); // Copying 4 bytes from i position to temp
			ids[i / 4] = DataUtil.byteToInt(temp);
		}
		
		GameClient sender = program.findClient(client);
		List<GameClient> gameClients = program.getGameClients();

		// Iterate through game clients, remove from sight listed ids
		for(GameClient gc: gameClients){
			for(int i = 0; i < ids.length; i++){
				if (gc.getId() == ids[i]){
					//sender.getWhoCanSeeMe().remove(gc);
					break;
				}
			}
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
