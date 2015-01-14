package program.main;

import core.board.*;
import core.filters.LoggedFilter;
import core.handlers.*;
import core.handlers.admin.AdminMessageHandler;
import core.handlers.admin.CheckCredentialsAdminHandler;
import core.handlers.admin.GetPlayerInfoAdminHandler;
import core.main.*;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.FileUtils;
import program.main.data.DataLoader;
import program.main.database.Database;
import shared.board.data.*;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.File;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Program {
	public static final byte[] HEADER_RELIABLE = new byte[] { 127, 127 },
							   HEADER_ADMIN = new byte[] { 127, 126 },
							   HEADER_EXIT = new byte[] { 0, 0 },
							   HEADER_LOGIN = new byte[] { 0, 1 },
							   HEADER_STILL_ONLINE = new byte[] { 0, 2 },
							   HEADER_SAY = new byte[] { 0, 3 },
							   HEADER_QUERY = new byte[] { 0, 4 },
							   HEADER_REGISTER = new byte[] { 0, 5 },
							   HEADER_PLAYERS_IN_SIGHT = new byte[] { 0, 6 },
							   HEADER_GET_POSITION_INFO = new byte[] { 0, 7 },
							   HEADER_BATTLE_BEGINS = new byte[] { 0, 8 },
							   HEADER_ORDER_UPDATE = new byte[] { 0, 9 },
							   HEADER_BATTLE_MOVE_ORDER = new byte[] { 0, 10 },
							   HEADER_BATTLE_SKIP_TURN = new byte[] { 0, 11 },
							   HEADER_GET_INVENTORY = new byte[] { 0, 12 },
							   HEADER_PLACEMENT_FINISHED = new byte[] { 0, 13 },
							   HEADER_SWAP_CARDS = new byte[] { 0, 14 },
							   HEADER_GET_FACTION_INFO = new byte[] { 0, 15 },
							   HEADER_SPELL_CAST_ORDER = new byte[] { 0, 16 },
							   HEADER_SERVER_STATUS_REQUEST = new byte[] { 0, 17 },
							   HEADER_BATTLE_IS_OVER = new byte[] { 0, 18 },
							   HEADER_INSTANT_MOVE = new byte[] { 0, 19 },
							   HEADER_GET_PROFILE_INFO = new byte[] { 0, 20 },
							   HEADER_ATTACK_PLAYER = new byte[] { 0, 21 },
							   HEADER_REQUEST_DUEL = new byte[] { 0, 22 },
							   HEADER_ACCEPT_DUEL = new byte[] { 0, 23 },
							   HEADER_REJECT_DUEL = new byte[] { 0, 24 },
							   HEADER_CANCEL_DUEL = new byte[] { 0, 25 },
							   HEADER_DUEL_TIMEOUT = new byte[] { 0, 26 },
							   HEADER_BATTLE_TIMER_UPDATE = new byte[] { 0, 27 },
							   HEADER_CHANNEL_LIST = new byte[] { 0, 28 },
							   HEADER_ALL_CHANNELS_LIST = new byte[] { 0, 29 },
							   HEADER_JOIN_CHANNEL = new byte[] { 0, 30 },
							   HEADER_LEAVE_CHANNEL = new byte[] { 0, 31 },
							   HEADER_CARD_CAST_ORDER = new byte[] { 0, 32 },
							   HEADER_ADMIN_GET_PLAYER_INFO = new byte [] { 0, 33 },
							   HEADER_ADMIN_CHECK_CREDENTIALS = new byte [] { 0, 34 },
							   HEADER_REQUEST_TRADE = new byte[] { 0, 35 },
							   HEADER_TRADE_TIMEOUT = new byte[] { 0, 36 },
							   HEADER_REJECT_TRADE = new byte[] { 0, 37 },
							   HEADER_CANCEL_TRADE = new byte[] { 0, 38 },
							   HEADER_ACCEPT_TRADE = new byte[] { 0, 39 },
							   HEADER_AUTHORIZE_TRADE = new byte[] { 0, 40 },
							   HEADER_COMPLETE_TRADE = new byte[] { 0, 41 },
							   HEADER_TRADE_OFFER_ITEM = new byte[] { 0, 42 },
							   HEADER_BATTLE_PLACE_ORDER = new byte[] { 0, 43 },
							   HEADER_BATTLE_PICK_ORDER = new byte[] { 0, 44 },
							   HEADER_GET_ITEMS = new byte[] { 0, 45 },
							   HEADER_GET_CHARACTER_INFO = new byte[] { 0, 46 };

	public static final Charset UTF_8 = Charset.forName("UTF-8");

	protected LocalServer localServer;

	protected GroovyScriptEngine groovyScriptEngine;

	private List<GameClient> gameClients;
	private Database database;

	protected PathingMap pathingMap;
	protected ClusterGrid clusterGrid;

	protected Map<CardMaster, GameClient> cardMasterGameClientMap;
	protected Map<CardMaster, Npc> cardMasterNpcMap;
	protected Map<Integer, ServerCardMaster> idCardMasterMap;
	protected Map<Integer, GameClient> idGameClientMap;
	protected Map<Integer, Npc> idNpcMap;
	protected Map<Integer, UnitData> unitDataMap;
	protected Map<String, BuffData> buffDataMap;
	protected Map<String, SpellData> spellDataMap;
	protected Map<String, PassiveData> passiveDataMap;
	protected Map<String, CardSpellData> cardSpellDataMap;
	protected Map<Client, GameClient> clientGameClientMap;

	private static Program instance;

	private TradingController tradingController;
	private FactionController factionController;
	private BattleController battleController;
	private DuelController duelController;
	private ChatController chatController;
	private MapController mapController;

	public static Program getInstance(){
		if (instance == null)
			instance = new Program();
		return instance;
	}

	public LocalServer startServer() throws SocketException{
		Client.setStrictEquals(false);
		LocalServer server = new LocalServer(3637);
		server.addPacketHandler(new ExitMessageHandler(HEADER_EXIT));
		server.addPacketHandler(new LoginMessageHandler(HEADER_LOGIN));
		server.addPacketHandler(new SayMessageHandler(HEADER_SAY));
		server.addPacketHandler(new StillOnlineMessageHandler(HEADER_STILL_ONLINE));
		server.addPacketHandler(new RegisterMessageHandler(HEADER_REGISTER));
		server.addPacketHandler(new GetPositionInfoMessageHandler(HEADER_GET_POSITION_INFO));
		server.addPacketHandler(new OrderUpdateMessageHandler(HEADER_ORDER_UPDATE));
		server.addPacketHandler(new BattleMoveMessageHandler(HEADER_BATTLE_MOVE_ORDER));
		server.addPacketHandler(new BattlePickMessageHandler(HEADER_BATTLE_PICK_ORDER));
		server.addPacketHandler(new BattlePlaceMessageHandler(HEADER_BATTLE_PLACE_ORDER));
		server.addPacketHandler(new SkipTurnMessageHandler(HEADER_BATTLE_SKIP_TURN));
		server.addPacketHandler(new GetInventoryMessageHandler(HEADER_GET_INVENTORY));
		server.addPacketHandler(new PlacementFinishedMessageHandler(HEADER_PLACEMENT_FINISHED));
		server.addPacketHandler(new CastSpellMessageHandler(HEADER_SPELL_CAST_ORDER));
		server.addPacketHandler(new ServerStatusRequestMessageHandler(HEADER_SERVER_STATUS_REQUEST));
		server.addPacketHandler(new GetProfileInfoMessageHandler(HEADER_GET_PROFILE_INFO));
		server.addPacketHandler(new AttackPlayerMessageHandler(HEADER_ATTACK_PLAYER));
		server.addPacketHandler(new RequestDuelMessageHandler(HEADER_REQUEST_DUEL));
		server.addPacketHandler(new AcceptDuelMessageHandler(HEADER_ACCEPT_DUEL));
		server.addPacketHandler(new RejectDuelMessageHandler(HEADER_REJECT_DUEL));
		server.addPacketHandler(new CancelDuelMessageHandler(HEADER_CANCEL_DUEL));
		server.addPacketHandler(new CastCardSpellMessageHandler(HEADER_CARD_CAST_ORDER));
		server.addPacketHandler(new RequestTradeMessageHandler(HEADER_REQUEST_TRADE));
		server.addPacketHandler(new AcceptTradeMessageHandler(HEADER_ACCEPT_TRADE));
		server.addPacketHandler(new RejectTradeMessageHandler(HEADER_REJECT_TRADE));
		server.addPacketHandler(new CancelTradeMessageHandler(HEADER_CANCEL_TRADE));
		server.addPacketHandler(new GetItemsMessageHandler(HEADER_GET_ITEMS));
		server.addPacketHandler(new GetCharacterInfoMessageHandler(HEADER_GET_CHARACTER_INFO));

		server.addPacketHandler(new ReliableMessageHandler(HEADER_RELIABLE));

		{
			AdminMessageHandler admin = new AdminMessageHandler(HEADER_ADMIN);

			admin.addPacketHandler(new GetPlayerInfoAdminHandler(HEADER_ADMIN_GET_PLAYER_INFO));
			admin.addPacketHandler(new CheckCredentialsAdminHandler(HEADER_ADMIN_CHECK_CREDENTIALS));

			server.addPacketHandler(admin);
		}

		server.addPacketFilter(new LoggedFilter());
		server.start();
		return server;
	}

	public MapController getMapController() {
		return mapController;
	}

	public GroovyScriptEngine getScriptEngine(){
		return groovyScriptEngine;
	}

	public PathingMap getPathingMap() {
		return pathingMap;
	}

	public SpellData getSpellDataById(String id){
		return spellDataMap.get(id);
	}

	public PassiveData getPassiveDataById(String id){
		return passiveDataMap.get(id);
	}

	public CardSpellData getCardSpellDataById(String id){
		return cardSpellDataMap.get(id);
	}

	public BuffData getBuffScriptById(String id){
		return buffDataMap.get(id);
	}

	public GameClient getGameClientById(int id){
		return idGameClientMap.get(id);
	}

	public ServerCardMaster getCardMasterById(int id){
		return idCardMasterMap.get(id);
	}

	public Npc getNpcById(int id){
		return idNpcMap.get(id);
	}

	public GameClient getGameClientByCardMaster(CardMaster cardMaster){
		return cardMasterGameClientMap.get(cardMaster);
	}

	public Npc getNpcByCardMaster(CardMaster cardMaster){
		return cardMasterNpcMap.get(cardMaster);
	}

	public void setPathingMap(PathingMap pathingMap) {
		this.pathingMap = pathingMap;
	}

	public Map<Integer, Npc> getIdNpcMap() {
		return idNpcMap;
	}

	public Map<CardMaster, Npc> getCardMasterNpcMap() {
		return cardMasterNpcMap;
	}

	public void addClient(GameClient client){
		clientGameClientMap.put(client.getClient(), client);
		idCardMasterMap.put(client.getCardMaster().getId(), client.getCardMaster());
		idGameClientMap.put(client.getId(), client);
		cardMasterGameClientMap.put(client.getCardMaster(), client);
		clusterGrid.updateCardMaster(client.getCardMaster());
		gameClients.add(client);

		mapController.sendWorldInfo(client);
	}

	public UnitData getUnitDataById(int id){
		return unitDataMap.get(id);
	}

	public GameClient findClient(Client client){
		return clientGameClientMap.get(client);
	}

	public TradingController getTradingController() {
		return tradingController;
	}

	public BattleController getBattleController() {
		return battleController;
	}

	public FactionController getFactionController() {
		return factionController;
	}

	public ChatController getChatController() {
		return chatController;
	}

	public Program() {
		idCardMasterMap = new ConcurrentHashMap<Integer, ServerCardMaster>();
		idGameClientMap = new ConcurrentHashMap<Integer, GameClient>();
		idNpcMap = new ConcurrentHashMap<Integer, Npc>();
		cardMasterGameClientMap = new ConcurrentHashMap<CardMaster, GameClient>();
		cardMasterNpcMap = new ConcurrentHashMap<CardMaster, Npc>();
		clientGameClientMap = new ConcurrentHashMap<Client, GameClient>();
		buffDataMap = new HashMap<String, BuffData>();
		spellDataMap = new HashMap<String, SpellData>();
		passiveDataMap = new HashMap<String, PassiveData>();
		cardSpellDataMap = new HashMap<String, CardSpellData>();
		unitDataMap = new HashMap<Integer, UnitData>();

		gameClients = new LinkedList<GameClient>();
	}

	public void start(){
		try {
			localServer = startServer();
			database = new Database("database/main");
			database.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		clusterGrid = new ClusterGrid(256, 256); // Just some test magic values

		// Loading data
		DataLoader dataLoader = new DataLoader();

		groovyScriptEngine = dataLoader.loadScriptEngine();

		DataUtil.loadDataList("res/units/datalist", Integer.class, UnitData.class, unitDataMap);
		DataUtil.loadDataList("res/spells/unit/datalist", String.class, SpellData.class, spellDataMap);
		DataUtil.loadDataList("res/spells/hero/datalist", String.class, CardSpellData.class, cardSpellDataMap);
		DataUtil.loadDataList("res/buffs/datalist", String.class, BuffData.class, buffDataMap);

		dataLoader.loadPathingMapFromFileSystem();

		factionController = new FactionController();
		factionController.createFactions();

		tradingController = new TradingController();
		battleController = new BattleController();
		duelController = new DuelController();
		chatController = new ChatController();
		mapController = new MapController();

		mapController.loadNpcTable();
		chatController.createChannel("Main");

		loadScripts();

		// Run periodic execution
		// All data has to be loaded in advance to run this method
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new UpdateClientsRunnable(), 20, 20, TimeUnit.MILLISECONDS);
	}

	public void populate(Database database){
		this.database = database;

		factionController = new FactionController();
		factionController.createFactions();

		tradingController = new TradingController();
		battleController = new BattleController();
		duelController = new DuelController();
		chatController = new ChatController();
		mapController = new MapController();

		try {
			List<String> names = FileUtils.readLines(new File("res/names.txt"));

			for (String name: names){
				Npc npc = mapController.createNpc(name);

				ServerCardMaster cardMaster = npc.getCardMaster();

				cardMaster.getHero().setX(256 + (float) Math.random() * 2000f);
				cardMaster.getHero().setY(256 + (float) Math.random() * 2000f);

				mapController.saveCardMaster(cardMaster);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadScripts(){
		for (String key: spellDataMap.keySet())
			getSpellDataById(key).compileScript(groovyScriptEngine, new Binding());

		for (String key: passiveDataMap.keySet())
			getSpellDataById(key).compileScript(groovyScriptEngine, new Binding());

		for (String key: cardSpellDataMap.keySet())
			getCardSpellDataById(key).compileScript(groovyScriptEngine, new Binding());

		for (String key: buffDataMap.keySet())
			getBuffScriptById(key).compileScript(groovyScriptEngine, new Binding());

		for (int key: unitDataMap.keySet())
			getUnitDataById(key).compileScript(groovyScriptEngine, new Binding());
	}

	public DuelController getDuelController() {
		return duelController;
	}

	public ClusterGrid getClusterGrid() {
		return clusterGrid;
	}

	public List<GameClient> getGameClients() {
		return gameClients;
	}

	public LocalServer getLocalServer() {
		return localServer;
	}

	private class UpdateClientsRunnable extends Thread{
		public void run() {
			try{
				mapController.update();
				tradingController.update();
				battleController.update();
				duelController.update();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public Database getDatabase() {
		return database;
	}

	public static void main(String[] args) {
		Program.getInstance().start();
	}
}