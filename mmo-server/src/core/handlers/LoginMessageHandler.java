package core.handlers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import core.exceptions.IncorrectHeaderException;
import core.main.*;
import program.main.database.Database;
import program.main.Program;
import program.main.ReliablePacketManager;
import program.main.database.entities.CardMasterEntity;
import program.main.database.entities.GameClientEntity;

import java.security.MessageDigest;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

public class LoginMessageHandler extends PacketHandler{
	private Program program;
	private static Packet packetLoginFailed, packetAlreadyLogged;

	static {
		try {
			packetLoginFailed = new Packet();
			packetAlreadyLogged = new Packet();

			packetLoginFailed.setData(Program.HEADER_LOGIN, new byte[] { 1 });
			packetAlreadyLogged.setData(Program.HEADER_LOGIN, new byte[] { 2 });
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
}

	public LoginMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		// Exit if client is already logged in
		if (program.findClient(client) != null)
			return;
		
		/** First two bytes contain login/password length
		 *  Other bytes contain login and password themselves
		 */
		byte b[] = data.getData();
		
		// Exit if there is not enough data
		if (b.length < 2)
			return;
		
		// Name/pass len (in bytes)
		int nameLen = b[0],
			passLen = b[1];
		
		// Exit if array length is incorrect
		if (b.length != 2 + nameLen + passLen)
			return;
		
		byte bname[] = Arrays.copyOfRange(b, 1, 2 + nameLen),
			 bpass[] = Arrays.copyOfRange(b, 2 + nameLen, b.length);
		
		try {
			String name = new String(bname, "UTF-8"),
				   pass = new String(bpass, "UTF-8");
			
			name = name.trim().replaceAll("\\s{2,}", " "); // Replacing 2+ spaces in name with one space and trimming it
			
			// Exit if name doesn't match pattern
			if (!name.matches("[a-zA-Z0-9\\-_ ]{3,20}")) // Only latin letters, numbers, spaces, - or _, from 3 to 20 symbols
				return;
			
			// Exit if password is too big or short
			if (pass.length() > 32 || pass.length() < 3)
				return;
			
			// Exit if there is no access to database
			Database db = program.getDatabase();

			// Getting password MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] passEncoded = md.digest(bpass);
			
			// Translating encoded array into normal hex representation
			StringBuilder sb = new StringBuilder();
			for (byte passEncodedByte : passEncoded)
				sb.append(Integer.toHexString((passEncodedByte & 0xFF) | 0x100).substring(1, 3));
	        
	        // Final password
	        pass = sb.toString();

			QueryBuilder<CardMasterEntity,Integer> masterBuilder = db.getCardMasterDao().queryBuilder();
			QueryBuilder<GameClientEntity, Integer> clientBuilder = db.getGameClientDao().queryBuilder();

			clientBuilder.where().eq(GameClientEntity.PASSWORD_FIELD, pass);
			masterBuilder.where().eq(CardMasterEntity.NAME_FIELD, name);

			GameClientEntity entity = clientBuilder.leftJoin(masterBuilder).queryForFirst();

			if (entity == null){
				ReliablePacketManager.sendPacket(localServer, client, packetLoginFailed); // Send fail message back to user
				return;
			}

			CardMasterEntity masterEntity = entity.getCardMaster();

			int id = masterEntity.getId();

			// Exit if user is already logged in and send back special message
			if (program.getCardMasterById(id) != null){
				ReliablePacketManager.sendPacket(localServer, client, packetAlreadyLogged);
				return;
			}

			GameClient loggedUser = new GameClient(client);

			CardMaster cardMaster = loggedUser.getCardMaster();

			cardMaster.setId(id);
			cardMaster.setName(masterEntity.getName());
			cardMaster.setData(masterEntity.getData());
			cardMaster.getInventory().loadItems();

			program.addClient(loggedUser);
			program.getChatController().joinChannel(loggedUser, 0);

			byte result[] = new byte[5];
			// Set success packet client id data
			System.arraycopy(DataUtil.intToByte(id), 0, result, 1, 4);

			ReliablePacketManager.sendPacket(localServer, client, new Packet(Program.HEADER_LOGIN, result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
