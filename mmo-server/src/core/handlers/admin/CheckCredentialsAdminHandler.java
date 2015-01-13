package core.handlers.admin;

import core.exceptions.IncorrectHeaderException;
import core.main.*;
import program.main.Program;

/**
 * @author doc
 */
public class CheckCredentialsAdminHandler extends PacketHandler {
	private Program program;

	public CheckCredentialsAdminHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		try {
			localServer.send(client, new Packet(Program.HEADER_ADMIN_CHECK_CREDENTIALS, new byte[0]));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}