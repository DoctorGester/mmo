package core.ui.menu;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import core.exceptions.IncorrectPacketException;
import core.main.Packet;
import program.main.Program;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.core.Screen;

import java.net.InetSocketAddress;

/**
 * @author doc
 */
public class ServerSelectionButton extends ButtonAdapter{
	private InetSocketAddress server;

	public ServerSelectionButton(Screen screen, Vector2f position, Vector2f dimensions, InetSocketAddress server) {
		super(screen, position, dimensions);
		this.server = server;
	}

	public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
		Program.getInstance().connectTo(server);

		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_SERVER_STATUS_REQUEST));
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}
}
