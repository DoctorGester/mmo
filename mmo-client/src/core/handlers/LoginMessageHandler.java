package core.handlers;

import core.graphics.scenes.Scenes;
import core.main.*;
import core.ui.UI;
import core.ui.menu.MenuUIState;
import program.main.Program;
import program.main.SceneUtil;
import shared.other.DataUtil;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class LoginMessageHandler extends PacketHandler{
	private Program program;
	
	private static String responses[] = {
		"Logged in. Welcome",
		"Username/Password combination not found",
		"User is already logged in"
	};

	public LoginMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		final byte b[] = data.getData().clone();
		// Checking if data size is correct (5 for loginSuccess packet)
		if (b.length != 1 && b.length != 5)
			return;

		program.getMainFrame().enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				int responseCode = b[0];

				// For successful login
				if (responseCode == 0){
					program.setMainClientId(DataUtil.byteToInt(Arrays.copyOfRange(b, 1, 5)));
					program.setClientState(Program.STATE_GLOBAL_MAP);
					program.getMainFrame().setUIState(UI.STATE_MAP_MAIN);
					program.getMainFrame().setScene(Scenes.MAIN_MAP);
				} else {
					SceneUtil.getUI(UI.STATE_MAIN_MENU, MenuUIState.class).setMessage(responses[responseCode], true);
				}

				return null;
			}
		});

	}


}
