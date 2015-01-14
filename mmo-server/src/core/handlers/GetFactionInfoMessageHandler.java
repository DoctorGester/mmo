package core.handlers;

import core.main.*;
import program.main.FactionController;
import program.main.Program;
import program.main.ReliablePacketManager;
import shared.map.Faction;

import java.nio.charset.Charset;
import java.util.Arrays;

@Deprecated
public class GetFactionInfoMessageHandler extends PacketHandler{

	private Program program;

	public GetFactionInfoMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		GameClient sender = program.findClient(client);
		
		// Exit if client is not logged in
		if (sender == null)
			return;
		
		// Every byte is faction id
		byte data[] = packet.getData();

		// Exit if packet length is incorrect
		if (data.length == 0)
			return;

        byte tempBytes[] = new byte[512];

		FactionController controller = program.getFactionController();
		Faction factions[] = controller.getFactions().toArray(new Faction[controller.getFactions().size()]);

        int dataSize = factions.length + 1; // First bytes are length determiners. The very first byte holds amount of factions.

        // Exit if client is requesting some weird stuff
        if (data.length > factions.length)
            return;

        tempBytes[0] = (byte) factions.length;

		int index = 0;
		for(byte id: data){
			index++;
			Faction faction = controller.getFactionById(id);
			if (faction == null)
				continue;
			tempBytes[index] = (byte) (faction.getName().getBytes().length + 1);
		}

        for(byte id: data){
            Faction faction = controller.getFactionById(id);
            if (faction == null)
                continue;
            tempBytes[dataSize++] = id;

            byte name[] = faction.getName().getBytes(Charset.forName("UTF-8"));
            System.arraycopy(name, 0, tempBytes, dataSize, name.length);
            dataSize += name.length;
        }

		// Sending all data in advance
		byte result[] = Arrays.copyOf(tempBytes, dataSize);

		ReliablePacketManager.sendPacket(localServer, client, Program.HEADER_GET_FACTION_INFO, result);
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
