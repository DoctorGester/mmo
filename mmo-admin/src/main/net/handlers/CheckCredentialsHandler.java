package main.net.handlers;

import core.main.*;
import main.core.Program;

/**
 * @author doc
 */
public class CheckCredentialsHandler extends PacketHandler {
	public CheckCredentialsHandler(byte[] header) {
		super(header);
	}

	public void handle(LocalServer localServer, Client client, Packet data) {}

	public void handle(LocalClient localClient, Packet data) {
		Program.getInstance().completeAuthorization();
	}
}
