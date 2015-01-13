package core.ui;

import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import core.exceptions.IncorrectHeaderException;
import core.exceptions.IncorrectPacketException;
import core.graphics.MainFrame;
import core.graphics.scenes.MapScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import core.ui.map.MapUIState;
import program.main.Program;
import program.main.Util;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author doc
 */
public class MapController implements ActionListener {
	private final Set<CardMaster> undefinedPlayers = new HashSet<CardMaster>();

	public MapController(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new RequestPlayersTask(), 50, 50, TimeUnit.MILLISECONDS);
	}

	public void requestTradePlayer(CardMaster to){
		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_REQUEST_TRADE, DataUtil.intToByte(to.getId())));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void requestDuelPlayer(CardMaster to){
		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_REQUEST_DUEL, DataUtil.intToByte(to.getId())));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void acceptDuel(CardMaster to){
		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_ACCEPT_DUEL, DataUtil.intToByte(to.getId())));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void rejectDuel(CardMaster to){
		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_REJECT_DUEL, DataUtil.intToByte(to.getId())));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void cancelDuel(){
		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_CANCEL_DUEL));
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}

	public void requestAttackPlayer(CardMaster cardMaster){
		int id = cardMaster.getId();

		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_ATTACK_PLAYER, DataUtil.intToByte(id)));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void requestProfileInfo(CardMaster cardMaster){
		requestProfileInfo(cardMaster.getId());
	}

	public void requestProfileInfo(int id){
		try {
			Program.getInstance().getLocalClient().send(new Packet(Program.HEADER_GET_PROFILE_INFO, DataUtil.intToByte(id)));
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void requestInitialInfo(CardMaster player){
		synchronized (undefinedPlayers){
			undefinedPlayers.add(player);
		}
	}

	public void onAction(String name, boolean pressed, float tpf) {
		MainFrame mainFrame = Program.getInstance().getMainFrame();
		LocalClient localClient = Program.getInstance().getLocalClient();

		InputManager im = mainFrame.getInputManager();

		if (name.equals("leftClick") && !pressed){
			Vector2f clickPosition = im.getCursorPosition();
			Vector3f worldPosition = mainFrame.getCamera().getWorldCoordinates(clickPosition, 0f).clone();
			Vector3f dir = mainFrame.getCamera().getWorldCoordinates(clickPosition, 1f).subtractLocal(worldPosition).normalizeLocal();

			Ray ray = new Ray(worldPosition, dir);
			CollisionResults results = new CollisionResults();

			Util.getScene(Scenes.MAIN_MAP, MapScene.class).getTerrain().collideWith(ray, results);

			if (results.size() == 0)
				return;

			Vector3f collisionPoint = results.getCollision(0).getContactPoint();
			Vector2f orderPosition = new Vector2f(collisionPoint.x, collisionPoint.z).multLocal(10f);

			try {
				ByteOutputStream bytes = new ByteOutputStream();
				DataOutputStream stream = new DataOutputStream(bytes);

				stream.write(Hero.ORDER_MOVE);
				stream.writeInt((int) orderPosition.x);
				stream.writeInt((int) orderPosition.y);

				localClient.send(new Packet(Program.HEADER_ORDER_UPDATE, bytes.getBytes()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (name.equals("rightClick") && !pressed){
			Vector2f clickPosition = im.getCursorPosition();

			CardMaster clicked = Util.getScene(Scenes.MAIN_MAP, MapScene.class).getCardMasterAtCursor(clickPosition);

			if (clicked != null && clicked != Program.getInstance().getMainPlayer())
				Util.getUI(UI.STATE_MAP_MAIN, MapUIState.class).showMenu(clicked, clickPosition);
			else
				Util.getUI(UI.STATE_MAP_MAIN, MapUIState.class).hideMenu();
		}
	}

	private class RequestPlayersTask implements Runnable {
		private int counter;

		private static final int MAX_REQUEST_LENGTH = 16;

		@Override
		public void run() {
			synchronized (undefinedPlayers){
				if (undefinedPlayers.size() == 0)
					return;

				if (++counter > 4){
					counter = 0;

					for (CardMaster player: Program.getInstance().getVisiblePlayers())
						if (!player.isInitialized())
							undefinedPlayers.add(player);
				}

				int index = 0;
				int idArray[] = new int[Math.min(undefinedPlayers.size(), MAX_REQUEST_LENGTH)];

				for (CardMaster player: new HashSet<CardMaster>(undefinedPlayers)){
					idArray[index++] = player.getId();

					undefinedPlayers.remove(player);

					if (index == MAX_REQUEST_LENGTH)
						break;
				}

				byte request[] = DataUtil.intToVarInt(idArray);

				try {
					LocalClient localClient = Program.getInstance().getLocalClient();

					localClient.send(new Packet(Program.HEADER_GET_POSITION_INFO, request));
					localClient.send(new Packet(Program.HEADER_GET_CHARACTER_INFO, request));
				} catch (IncorrectHeaderException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
