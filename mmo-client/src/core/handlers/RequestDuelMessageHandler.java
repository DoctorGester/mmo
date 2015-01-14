package core.handlers;

import core.main.*;
import core.ui.UI;
import core.ui.map.RequestsUIState;
import core.ui.map.requests.DuelRequest;
import core.ui.map.requests.Request;
import program.main.Program;
import program.main.SceneUtil;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.util.concurrent.Callable;

public class RequestDuelMessageHandler extends PacketHandler{
	private Program program;

	public RequestDuelMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}

	public void handle(LocalClient localClient, Packet data) {
		final int ids[] = DataUtil.varIntsToInts(data.getData());

		Program.getInstance().getMainFrame().enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				int from = ids[0],
					to = ids[1];

				CardMaster sender = program.getMainPlayer();

				RequestsUIState uiState = SceneUtil.getUI(UI.STATE_REQUESTS, RequestsUIState.class);

				Request request = new DuelRequest(program.getVisiblePlayerById(from), program.getVisiblePlayerById(to));

				if (sender.getId() == to)
					uiState.addIncomingRequest(request);

				if (sender.getId() == from)
					uiState.addOutgoingRequest(request, true);

				return null;
			}
		});

	}
}
