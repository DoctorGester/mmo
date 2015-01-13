package core.filters;

import core.main.Client;
import core.main.LocalServer;
import core.main.Packet;
import core.main.PacketFilter;
import program.main.Program;

public class LoggedFilter implements PacketFilter{
	private Program program;
	
	public LoggedFilter(){
		this.program = Program.getInstance();
	}
	
	private boolean hdr(byte h[], byte h2[]){
		return (h[0] == h2[0] && h[1] == h2[1]);
	}
	
	public boolean filter(LocalServer server, Client client, Packet data) {
		byte b[] = data.getHeader();
		// Arrays.equals(byte, byte) is a bit slower than manual check
		return (hdr(b, Program.HEADER_LOGIN)
				|| hdr(b, Program.HEADER_REGISTER)
				|| hdr(b, Program.HEADER_EXIT)
				|| hdr(b, Program.HEADER_QUERY)
				|| hdr(b, Program.HEADER_RELIABLE)
				|| hdr(b, Program.HEADER_SERVER_STATUS_REQUEST)
				|| hdr(b, Program.HEADER_ADMIN)
				|| program.findClient(client) != null);
	}

}
