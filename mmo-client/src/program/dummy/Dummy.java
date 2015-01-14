package program.dummy;

import core.exceptions.IncorrectHeaderException;
import core.exceptions.IncorrectPacketException;
import core.main.*;
import program.main.Program;
import core.handlers.ReliableMessageHandler;
import shared.other.DataUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author doc
 */
public class Dummy {

	private class UpdateThread implements Runnable{

		private int tick = 0;

		@Override
		public void run() {
			try {
				if (tick % 100 == 0)
					localClient.send(new Packet(Program.HEADER_STILL_ONLINE));

				tick++;
			} catch (IncorrectPacketException e) {
				e.printStackTrace();
			}
		}
	}

	private LocalClient localClient;

	private String login = "",
				   pass = "";
	private String ip = "127.0.0.1";
	private int port = 3637;

	private void parseArgs(String ... args){
		int user = -1;

		for (String arg: args){
			if (arg.equalsIgnoreCase("-u") && user == -1){
				user = 0;
				continue;
			}
			if (user == 0){
				login = arg;
				user++;
				continue;
			}
			if (user == 1){
				pass = arg;
				user = -1;
			}
		}
	}

	private void login() throws Exception{
		byte bname[] = login.getBytes("UTF-8");
		byte bpass[] = pass.getBytes("UTF-8");
		byte data[] = new byte[2 + bname.length + bpass.length];

		data[0] = (byte) bname.length;
		data[1] = (byte) bpass.length;

		System.arraycopy(bname, 0, data, 2, bname.length);
		System.arraycopy(bpass, 0, data, 2 + bname.length, bpass.length);

		localClient.send(new Packet(Program.HEADER_LOGIN, data));
	}

	private void createClient() throws Exception{
		localClient = new LocalClient(0, new InetSocketAddress(ip, port));

		localClient.addPacketHandler(new ReliableMessageHandler(new byte[] { 127, 127 }));
		localClient.addPacketHandler(new PacketHandler(Program.HEADER_LOGIN) {
			public void handle(LocalServer localServer, Client client, Packet data) {}
			public void handle(LocalClient localClient, Packet data) {

			}
		});

		localClient.addPacketHandler(new PacketHandler(Program.HEADER_REQUEST_TRADE) {
			public void handle(LocalServer localServer, Client client, Packet packet) {}
			public void handle(LocalClient localClient, Packet packet) {
				send(Program.HEADER_ACCEPT_TRADE, DataUtil.intToByte(DataUtil.varIntsToInts(packet.getData())[0]));
			}
		});
	}

	private void startThread(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new UpdateThread(), 20, 20, TimeUnit.MILLISECONDS);
	}

	private void createQuickHandler(byte[] header, final byte[] answer){
		createQuickHandler(header, header, answer);
	}

	private void createQuickHandler(byte[] header, final byte[] answerHeader, final byte[] answer){
		localClient.addPacketHandler(new PacketHandler(header) {
			public void handle(LocalServer localServer, Client client, Packet packet) {}
			public void handle(LocalClient localClient, Packet packet) {
				try {
					localClient.send(new Packet(answerHeader, answer));
				} catch (IncorrectHeaderException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void send(byte[] header, byte[] data){
		try {
			localClient.send(new Packet(header, data));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public Dummy(String ... args){
		try {
			parseArgs(args);
			createClient();
			login();
			startThread();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String ... args){
		new Dummy(args);
	}
}
