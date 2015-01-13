package program.main.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import program.main.Program;
import program.main.database.entities.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


public class Database {
	private String path;
	private ConnectionSource source;

	private Dao<NpcEntity, Integer> npcDao;
	private Dao<ItemEntity, Integer> itemDao;
	private Dao<GameClientEntity, Integer> gameClientDao;
	private Dao<CardMasterEntity, Integer> cardMasterDao;
	private Dao<MatchEntity, Integer> matchDao;

	public Database(String path){
		this.path = path;
	}
	
	public void connect() throws SQLException{
		source = new JdbcPooledConnectionSource("jdbc:hsqldb:" + path);

		npcDao = DaoManager.createDao(source, NpcEntity.class);
		itemDao = DaoManager.createDao(source, ItemEntity.class);
		matchDao = DaoManager.createDao(source, MatchEntity.class);
		gameClientDao = DaoManager.createDao(source, GameClientEntity.class);
		cardMasterDao = DaoManager.createDao(source, CardMasterEntity.class);
	}

	public void create(){
		try {
			TableUtils.dropTable(source, GameClientEntity.class, true);
			TableUtils.dropTable(source, CardMasterEntity.class, true);
			TableUtils.dropTable(source, MatchEntity.class, true);
			TableUtils.dropTable(source, ItemEntity.class, true);
			TableUtils.dropTable(source, NpcEntity.class, true);

			TableUtils.createTable(source, GameClientEntity.class);
			TableUtils.createTable(source, CardMasterEntity.class);
			TableUtils.createTable(source, MatchEntity.class);
			TableUtils.createTable(source, ItemEntity.class);
			TableUtils.createTable(source, NpcEntity.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void populate(){
		Program.getInstance().populate(this);
	}

	public ConnectionSource getSource() {
		return source;
	}

	public Object[] fetch(ResultSet rs) throws SQLException{
		ResultSetMetaData meta = rs.getMetaData();
	    int colCount = meta.getColumnCount();
	    Object[] row = new Object[colCount];
	    for(int i = 0; i < colCount; i++)
	    	row[i] = rs.getObject(meta.getColumnName(i + 1));
	    return row;
	}

	public void close(){
		try {
			source.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Dao<NpcEntity, Integer> getNpcDao() {
		return npcDao;
	}

	public Dao<ItemEntity, Integer> getItemDao() {
		return itemDao;
	}

	public Dao<MatchEntity, Integer> getMatchDao() {
		return matchDao;
	}

	public Dao<GameClientEntity, Integer> getGameClientDao() {
		return gameClientDao;
	}

	public Dao<CardMasterEntity, Integer> getCardMasterDao() {
		return cardMasterDao;
	}

	public static void main(String args[]){
		try {
			Database database = new Database("database/main");
			database.connect();
			database.create();
			database.populate();
			database.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}