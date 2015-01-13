package core.handlers;

import core.main.*;
import program.main.Program;
import program.main.ReliablePacketManager;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class OrderUpdateMessageHandler extends PacketHandler{

	private Program program;

	public OrderUpdateMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		try {
			DataInputStream out = DataUtil.stream(data.getData());

			int order = out.readByte();

			GameClient gameClient = program.findClient(client);

			if (gameClient == null)
				return;

			CardMaster cardMaster = gameClient.getCardMaster();

			if (cardMaster.getState() != CardMaster.STATE_IN_GLOBAL_MAP)
				return;

			Hero hero = cardMaster.getHero();

			switch(order){
				case Hero.ORDER_STOP:
					hero.setOrder(order);
					break;

				case Hero.ORDER_MOVE:
					HeroPath heroPath = new HeroPath(hero, 2, out.readInt(), out.readInt(), program.getPathingMap(), 50);

					if (!heroPath.find())
						return;

					hero.setOrder(order);
					hero.setPath(heroPath.getPath());

					break;

				default:
					return;
			}

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(1); // Information batch size
			stream.write(DataUtil.intToByte(cardMaster.getId()));
			stream.write(cardMaster.getPositionInfo());

			byte orderData[] = stream.toByteArray();

			Set<GameClient> whoCanSeeMe = cardMaster.getWhoCanSeeMe();
			for(GameClient cl: whoCanSeeMe)
				ReliablePacketManager.sendPacket(localServer, cl.getClient(), Program.HEADER_GET_POSITION_INFO, orderData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
