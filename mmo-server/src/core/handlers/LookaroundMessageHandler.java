package core.handlers;

import core.exceptions.IncorrectHeaderException;
import core.main.*;
import nf.fr.eraasoft.pool.PoolException;
import program.main.Program;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.util.Arrays;
import java.util.Set;

@Deprecated
public class LookaroundMessageHandler extends PacketHandler{

	private Program program;
	
	public LookaroundMessageHandler(byte[] header) {
		super(header);
	}
	
	public LookaroundMessageHandler(Program program, byte header[]){
		this(header);
		this.program = program;
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		try {
			GameClient sender = program.findClient(client);
			
			// Exit if this client is not logged in
			if (sender == null)
				return;
			
			Set<ServerCardMaster> cardMastersInSight = sender.getPlayersInSight();
			int ids[] = new int[cardMastersInSight.size()];
			int c = 0;
			for(CardMaster cardMaster: cardMastersInSight){
				ids[c++] = cardMaster.getId();
			}
			
			// Sorting id array so we could get valid hash
			Arrays.sort(ids);

			// Exit if previously sent array is equal to this one
			int hash = Arrays.hashCode(ids);
			//if (sender.getLookaroundHash() == hash)
			//	return;
			
			// Update hash to actual value
			//sender.setLookaroundHash(hash);
			
			// If arrays differ, convert all ids to bytes and send them
			byte b[] = new byte[c * 4]; // int is 4 bytes long
			for(int i = 0; i < c; i++){
				byte d[] = DataUtil.intToByte(ids[i]);
				System.arraycopy(d, 0, b, i * 4, 4);	
			}
			
			Packet p = Packet.getPool().getObj();
			p.setData(Program.HEADER_PLAYERS_IN_SIGHT, b);
			localServer.send(client, p);
			Packet.getPool().returnObj(p);
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		} catch (PoolException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
