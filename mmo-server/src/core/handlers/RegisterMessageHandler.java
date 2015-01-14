package core.handlers;

import com.j256.ormlite.dao.Dao;
import core.exceptions.IncorrectHeaderException;
import core.main.*;
import shared.items.Item;
import core.main.ItemDatabase;
import shared.items.types.CardItem;
import program.main.database.Database;
import program.main.Program;
import program.main.database.entities.CardMasterEntity;
import program.main.database.entities.GameClientEntity;
import shared.map.CardMaster;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RegisterMessageHandler extends PacketHandler{
	private Program program;
	private static Packet packetRegisterSuccess,
						  packetUserAlreadyExists;
	
	static{
		try {
			packetRegisterSuccess = new Packet();
			packetUserAlreadyExists = new Packet();
			
			packetRegisterSuccess.setData(Program.HEADER_REGISTER, new byte[]{0});
			packetUserAlreadyExists.setData(Program.HEADER_REGISTER, new byte[]{1});
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public RegisterMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
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
			if (!name.matches("[a-zA-Z0-9-_ ]{3,20}")) // Only latin letters, numbers, spaces, - or _, from 3 to 20 symbols
				return;
			
			// Exit if password is too big or short
			if (pass.length() > 32 || pass.length() < 3)
				return;
			
			// Exit if there is no access to database
			Database db = program.getDatabase();

			Dao<CardMasterEntity,Integer> cardMasterDao = db.getCardMasterDao();
			Dao<GameClientEntity, Integer> gameClientDao = db.getGameClientDao();

			// Exit if name is already taken
			if (cardMasterDao.queryBuilder().where().eq(CardMasterEntity.NAME_FIELD, name).countOf() != 0){
				localServer.send(client, packetUserAlreadyExists); // Send fail message back to user
				return;
			}

			// Getting password MD5
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] passEncoded = md.digest(bpass);

			// Translating encoded array into normal hex representation
			StringBuilder passwordBuilder = new StringBuilder();
			for (byte passEncodedByte : passEncoded)
				passwordBuilder.append(Integer.toHexString((passEncodedByte & 0xFF) | 0x100).substring(1, 3));

			// Storing name and md5 password into database
			ServerCardMaster empty = new ServerCardMaster();

			// Creating card master entity
			CardMasterEntity masterEntity = new CardMasterEntity();

			masterEntity.setName(name);
			masterEntity.setData(empty.getData());

			cardMasterDao.create(masterEntity);

			// Creating game client
			GameClientEntity clientEntity = new GameClientEntity();

			clientEntity.setPassword(passwordBuilder.toString());
			clientEntity.setCardMaster(masterEntity);

			gameClientDao.create(clientEntity);

			// Creating default items
			empty.setId(masterEntity.getId());
			createDefaultInventory(empty);

			localServer.send(client, packetRegisterSuccess); // Send success message back to user
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createDefaultInventory(CardMaster cardMaster){
		List<Item> items = new LinkedList<Item>();

		CardItem customItem = new CardItem();
		customItem.setUnitId(1);
		items.add(new CardItem());
		items.add(customItem);

		customItem = new CardItem();
		customItem.setUnitId(2);
		items.add(customItem);

		customItem = new CardItem();
		customItem.setUnitId(3);
		items.add(customItem);

		customItem = new CardItem();
		customItem.setUnitId(4);
		items.add(customItem);

		for(Item item: items){
			item.setOwner(cardMaster);
			ItemDatabase.getInstance().registerItem(item);
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}
}
