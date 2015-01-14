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

public class RejectDuelMessageHandler extends PacketHandler{
	private Program program;

	public RejectDuelMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}

	public void handle(LocalClient localClient, Packet data) {
		final int id = DataUtil.byteToInt(data.getData());

		program.getMainFrame().enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				CardMaster sender = program.getMainPlayer();

				RequestsUIState uiState = SceneUtil.getUI(UI.STATE_REQUESTS, RequestsUIState.class);

				if (sender.getId() == id){
					uiState.removeOutgoingRequest(uiState.getSingleOutgoingRequest(sender));
				} else {
					Request request = new DuelRequest(program.getVisiblePlayerById(id), sender);
					uiState.removeIncomingRequest(request);
				}

				return null;
			}
		});
	}
}
