package core.handlers;

import core.main.*;
import core.ui.UI;
import core.ui.menu.MenuUIState;
import program.main.Program;
import program.main.SceneUtil;

import java.util.concurrent.Callable;

public class RegisterMessageHandler extends PacketHandler{
	private Program program;
	
	private static String responses[] = {
		"Registration succesful",
		"User already exists"
	};

	public RegisterMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		final byte b[] = data.getData().clone();
		// Checking if data size is correct
		if (b.length != 1)
			return;

		program.getMainFrame().enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				int responseCode = b[0];

                SceneUtil.getUI(UI.STATE_MAIN_MENU, MenuUIState.class).setMessage(responses[responseCode], responseCode != 0);

				return null;
			}
		});
	}


}
