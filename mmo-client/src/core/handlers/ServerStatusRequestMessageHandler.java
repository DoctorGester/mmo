package core.handlers;

import core.main.*;
import core.ui.UI;
import program.main.Program;

public class ServerStatusRequestMessageHandler extends PacketHandler{
	private Program program;

	public ServerStatusRequestMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		program.getMainFrame().setUIState(UI.STATE_MAIN_MENU);
	}


}
