package core.handlers.admin;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import core.handlers.admin.models.FindPlayerModel;
import core.main.*;
import program.main.database.Database;
import program.main.Program;
import program.main.database.entities.CardMasterEntity;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GetPlayerInfoAdminHandler extends PacketHandler{
	private Program program;

	public GetPlayerInfoAdminHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet packet) {
		String string = new String(packet.getData(), Program.UTF_8);

		Gson gson = AdminMessageHandler.createGson();

		FindPlayerModel model = gson.fromJson(string, FindPlayerModel.class);

		try {
			Database db = program.getDatabase();
			Dao<CardMasterEntity,Integer> cardMasterDao = db.getCardMasterDao();

			CardMasterEntity entity;

			if (model.getName() == null)
				entity = cardMasterDao.queryForId(model.getId());
			else
				entity = cardMasterDao.queryBuilder().where().eq(CardMasterEntity.NAME_FIELD, model.getName()).queryForFirst();

			if (entity == null)
				return;

			int id = entity.getId();
			String name = entity.getName();
			byte data[] = entity.getData();

			GameClient loggedUser = new GameClient(client);
			loggedUser.setId(id);
			loggedUser.getCardMaster().setName(name);
			loggedUser.getCardMaster().setData(data);
			loggedUser.getCardMaster().getInventory().loadItems();

			byte[] playerData = gson.toJson(loggedUser.getCardMaster()).getBytes(Program.UTF_8);

			localServer.send(client, new Packet(Program.HEADER_ADMIN_GET_PLAYER_INFO, playerData));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
