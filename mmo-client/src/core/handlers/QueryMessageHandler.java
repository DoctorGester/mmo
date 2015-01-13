package core.handlers;

import core.main.*;
import program.main.Program;

import java.io.UnsupportedEncodingException;

public class QueryMessageHandler extends PacketHandler{

	private Program program;

	public QueryMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
	}

	public void handle(LocalClient localClient, Packet data) {
		try {
			String spl[] = new String(data.getData(), "UTF-8").split("\n");
			for(String s: spl){
				System.out.println(s);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


}
