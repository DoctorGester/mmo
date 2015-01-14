package program.main;

import core.exceptions.IncorrectPacketException;
import core.main.Client;
import shared.other.DataUtil;
import core.main.LocalServer;
import core.main.Packet;
import nf.fr.eraasoft.pool.PoolException;

import java.util.*;

public class ReliablePacketManager {
	private static final List<ReliablePacket> packets;
	private static byte id;

	private static class ReliablePacket{
		public static final int REPEAT_TIME = 20,
								MAXIMUM_REPEAT_AMOUNT = 10;

		public LocalServer localServer;
		public Client client;
		public Packet packet;
		public int timeToRepeat = REPEAT_TIME,
				repeatAmount,
				   hash;
	}

	public static int getHashSum(byte data[]){
		return Arrays.hashCode(data);
	}

	public static void sendPacket(LocalServer server, Client client, byte header[], byte data[]){
		// Concatenating 3 arrays into one
		byte reliable[] = new byte[]{ Program.HEADER_RELIABLE[0], Program.HEADER_RELIABLE[1], id++ };
		byte raw[] = new byte[header.length + data.length + reliable.length];

		System.arraycopy(reliable, 0, raw, 0, reliable.length);
		System.arraycopy(header, 0, raw, reliable.length, header.length);
		System.arraycopy(data, 0, raw, reliable.length + header.length, data.length);

		ReliablePacket reliablePacket = new ReliablePacket();
		try {
			Packet packet = Packet.getPool().getObj();
			packet.setData(raw);

			reliablePacket.packet = packet;
			reliablePacket.client = client;
			reliablePacket.localServer = server;
			reliablePacket.hash = getHashSum(raw);

			packets.add(reliablePacket);

			server.send(client, packet);
		} catch (PoolException e) {
			e.printStackTrace();
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}

	public static void sendPacket(LocalServer server, Client client, Packet packet){
		sendPacket(server, client, packet.getHeader(), packet.getData());
	}

	public static void clientDisconnected(Client client){
        synchronized (packets){
            Iterator<ReliablePacket> iterator = packets.iterator();
            while(iterator.hasNext()){
                ReliablePacket packet = iterator.next();
                if (packet.client == client)
                    iterator.remove();
            }
        }
	}

	public static void handlePacket(Packet packet){
		int hash = DataUtil.byteToInt(packet.getData());

        synchronized (packets){
            Iterator<ReliablePacket> iterator = packets.iterator();
            while(iterator.hasNext()){
                ReliablePacket next = iterator.next();
                if (next.hash == hash){
                    iterator.remove();
                    Packet.getPool().returnObj(next.packet);
                    break;
                }
            }
        }
	}

	static {
		packets = Collections.synchronizedList(new LinkedList<ReliablePacket>());
		new Thread(){
			public void run(){
				while(!Thread.interrupted()){
                    synchronized (packets){
                        Iterator<ReliablePacket> iterator = packets.iterator();
                        while(iterator.hasNext()){
                            ReliablePacket reliablePacket = iterator.next();
                            reliablePacket.timeToRepeat--;
                            if (reliablePacket.timeToRepeat <= 0){
                                reliablePacket.timeToRepeat = ReliablePacket.REPEAT_TIME;
                                reliablePacket.localServer.send(reliablePacket.client, reliablePacket.packet);
                                reliablePacket.repeatAmount++;
                                if (reliablePacket.repeatAmount > ReliablePacket.MAXIMUM_REPEAT_AMOUNT){
                                    iterator.remove();
                                    Packet.getPool().returnObj(reliablePacket.packet);
                                }
                            }
                        }
                    }
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}