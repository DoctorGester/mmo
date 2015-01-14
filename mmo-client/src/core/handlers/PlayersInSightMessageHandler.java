package core.handlers;

import core.main.*;
import program.main.Program;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.util.HashSet;
import java.util.Set;

public class PlayersInSightMessageHandler extends PacketHandler{
	private Program program;

	public PlayersInSightMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

    private void handlePlayerIdArray(Set<Integer> idList, Set<ClientCardMaster> visible, Set<ClientCardMaster> invisible){
		Set<ClientCardMaster> newPlayers = new HashSet<ClientCardMaster>();

		for (Integer id: idList)
			newPlayers.add(program.getOrCreatePlayerById(id));

		Set<ClientCardMaster> clone = new HashSet<ClientCardMaster>(visible);

		clone.removeAll(newPlayers);
		invisible.addAll(clone);
		visible.retainAll(newPlayers);
		visible.addAll(newPlayers);
		invisible.removeAll(visible);
    }

	public void handle(LocalClient localClient, Packet data) {
		int idArray[] = DataUtil.varIntsToInts(data.getData());

		Set<ClientCardMaster> visiblePlayers = program.getVisiblePlayers(),
							  invisiblePlayers = program.getInvisiblePlayers();

		Set<Integer> idSet = new HashSet<Integer>(idArray.length);
		for (int id: idArray)
			idSet.add(id);

        handlePlayerIdArray(idSet, visiblePlayers, invisiblePlayers);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}
}