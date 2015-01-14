package program.main;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import core.exceptions.IncorrectPacketException;
import core.graphics.CardMasterSpatial;
import core.graphics.MainFrame;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.MapScene;
import core.graphics.scenes.Scenes;
import core.main.*;
import core.main.ClientInventory;
import core.ui.BattleController;
import core.ui.ChatController;
import core.ui.MapController;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import program.main.data.ClientDataLoader;
import core.handlers.*;
import shared.board.data.*;
import shared.map.CardMaster;
import shared.other.DataUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

public class Program {
	public static final int STATE_MENU = 0x00,
							STATE_GLOBAL_MAP = 0x01,
							STATE_BATTLE = 0x02;
	
	public static final byte[] HEADER_RELIABLE = new byte[] { 127, 127 },
							   HEADER_EXIT = new byte[] { 0, 0 },
							   HEADER_LOGIN = new byte[] { 0, 1 },
							   HEADER_STILL_ONLINE = new byte[] { 0, 2 },
							   HEADER_SAY = new byte[] { 0, 3 },
							   HEADER_QUERY = new byte[] { 0, 4 },
							   HEADER_REGISTER = new byte[] { 0, 5 },
							   HEADER_PLAYERS_IN_SIGHT_AROUND = new byte[] { 0, 6 },
							   HEADER_GET_POSITION_INFO = new byte[] { 0, 7 },
							   HEADER_BATTLE_BEGINS = new byte[] { 0, 8 },
							   HEADER_ORDER_UPDATE = new byte[] { 0, 9 },
							   HEADER_BATTLE_MOVE_ORDER = new byte[] { 0, 10 },
							   HEADER_BATTLE_SKIP_TURN = new byte[] {0, 11},
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

	private MainFrame mainFrame;

	private boolean onDestroy;

	private int clientState = -1;
	
	protected int mainId = -1; // Logged client id
	protected CardMaster mainPlayer;
	protected ClientInventory mainInventory = new ClientInventory();

	protected GroovyScriptEngine groovyScriptEngine;

	private LocalClient localClient;

	// Global map data
	private Set<ClientCardMaster> visiblePlayers, invisiblePlayers;

	private Map<Integer, ClientCardMaster> playerMap;
    private Map<Integer, Faction> factions;
    private Map<Integer, UnitData> unitDataMap;
	protected Map<String, BuffData> buffDataMap;
	protected Map<String, SpellData> spellDataMap;
	protected Map<String, PassiveData> passiveDataMap;
	protected Map<String, String> effectScriptMap;
	protected Map<String, CardSpellData> cardSpellDataMap;

	private MapController mapController;
	private BattleController battleController;
	private ChatController chatController;

	private Spatial testModel;

	private static Program instance;
	private UpdateLoop mainThread;

	private ClientDataLoader dataLoader = new ClientDataLoader();

	public static Program getInstance(){
		if (instance == null)
			instance = new Program();
		return instance;
	}

	public void loadTestModel(){
		testModel = mainFrame.getAssetManager().loadModel("res/models/zuus_model.dmx.mesh.xml");
		testModel.setLocalScale(0.03f);
		testModel.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

		testModel.updateModelBound();
	}

	public Spatial createTestModelInstance(){
		if (testModel == null)
			loadTestModel();
		return testModel.clone();
	}

	public Program() {
		visiblePlayers = new CopyOnWriteArraySet<ClientCardMaster>();
		invisiblePlayers = new CopyOnWriteArraySet<ClientCardMaster>();

        factions = new ConcurrentHashMap<Integer, Faction>();
		playerMap = new ConcurrentHashMap<Integer, ClientCardMaster>();
		buffDataMap = new HashMap<String, BuffData>();
		spellDataMap = new HashMap<String, SpellData>();
		passiveDataMap = new HashMap<String, PassiveData>();
		effectScriptMap = new HashMap<String, String>();
		cardSpellDataMap = new HashMap<String, CardSpellData>();
        unitDataMap = new HashMap<Integer, UnitData>();
	}

	public void start(){
		battleController = new BattleController();
		chatController = new ChatController();
		mapController = new MapController();

		mainFrame = new MainFrame();
		mainThread = new UpdateLoop(this);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(mainThread, 20, 20, TimeUnit.MILLISECONDS);

		dataLoader.loadMap();
		groovyScriptEngine = dataLoader.loadScriptEngine();
	}

	public ClientCardMaster createPlayer(int id){
		ClientCardMaster cardMaster = new ClientCardMaster();
		cardMaster.setId(id);

		playerMap.put(id, cardMaster);

		SceneUtil.getScene(Scenes.MAIN_MAP, MapScene.class)
				.addCardMasterSpatial(new CardMasterSpatial(createTestModelInstance(), cardMaster));

		mapController.requestInitialInfo(cardMaster);

		return cardMaster;
	}

	public ClientCardMaster getOrCreatePlayerById(int id){
		ClientCardMaster master = playerMap.get(id);

		if (master == null)
			master = createPlayer(id);

		return master;
	}

	public MapController getMapController() {
		return mapController;
	}

	public GroovyScriptEngine getScriptEngine(){
		return groovyScriptEngine;
	}

	public ClientDataLoader getDataLoader() {
		return dataLoader;
	}

	public void endGraphicsInit(){
		loadTestModel();
		initInput();
		setClientState(STATE_MENU);
		mainFrame.setScene(Scenes.MENU);

		DataUtil.loadDataList("res/units/datalist", Integer.class, UnitData.class, unitDataMap);
		DataUtil.loadDataList("res/spells/unit/datalist", String.class, SpellData.class, spellDataMap);
		DataUtil.loadDataList("res/spells/hero/datalist", String.class, CardSpellData.class, cardSpellDataMap);
		DataUtil.loadDataList("res/spells/passive/datalist", String.class, PassiveData.class, passiveDataMap);
		DataUtil.loadDataList("res/buffs/datalist", String.class, BuffData.class, buffDataMap);

		dataLoader.loadSpecialEffectsFromFileSystem();

		loadScripts();
	}

	public void loadScripts(){
		enqueue(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				for (String key: effectScriptMap.keySet())
					groovyScriptEngine.createScript(effectScriptMap.get(key), new Binding());

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

				return null;
			}
		});
	}

	public Script getEffectScriptById(String id){
		try {
			Binding binding = new Binding();
			binding.setVariable("assetManager", mainFrame.getAssetManager());
			binding.setVariable("root", mainFrame.getCurrentScene().getRoot());
			return getScriptEngine().createScript(effectScriptMap.get(id), binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ChatController getChatController() {
		return chatController;
	}

	public BattleController getBattleController() {
		return battleController;
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

    public Faction getFactionById(int id){
        return factions.get(id);
    }

    public void addFaction(Faction faction){
        factions.put(faction.getId(), faction);
    }

	public UnitData getUnitDataById(int id){
		return unitDataMap.get(id);
	}

	public void setOnDestroy(boolean onDestroy) {
		this.onDestroy = onDestroy;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public int getClientState() {
		return clientState;
	}
	
	public void setClientState(int clientState) {
		this.clientState = clientState;
	}
	
	public void setMainClientId(int id){
		mainId = id;
	}
	
	public CardMaster getMainPlayer(){
		return mainPlayer;
	}
	
	public PathingMap map;
	boolean mapData[] = new boolean[1024 * 1024];

    public void setMapData(boolean mapData[]){
        this.mapData = mapData;
    }

	public void updateMap(){
		map = new PathingMap(DataUtil.boolToByte(mapData), 1024, 1024);
		
		try {
			FileOutputStream fos = new FileOutputStream("map.map");
			fos.write(DataUtil.boolToByte(mapData));
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initInput(){
		InputManager im = mainFrame.getInputManager();
		im.addMapping("leftClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		im.addMapping("rightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		im.addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));

		BattleScene battleScene = SceneUtil.getScene(Scenes.BATTLE, BattleScene.class);
		battleScene.setActionListener(battleScene, "leftClick", "rightClick", "space");

		SceneUtil.getScene(Scenes.MAIN_MAP, MapScene.class).setActionListener(mapController, "leftClick", "rightClick");
	}

	public Map<Integer, Faction> getFactions() {
		return factions;
	}

	public Set<ClientCardMaster> getVisiblePlayers() {
		return visiblePlayers;
	}

	public Set<ClientCardMaster> getInvisiblePlayers() {
		return invisiblePlayers;
	}

	public ClientCardMaster getVisiblePlayerById(int id) {
		for (ClientCardMaster player: visiblePlayers)
			if (player.getId() == id)
				return player;
		return null;
	}

    public Map<String, String> getEffectScriptMap() {
        return effectScriptMap;
    }

    public void connectTo(InetSocketAddress inetSocketAddress) {
		try {
			localClient = new LocalClient(0, inetSocketAddress);
			localClient.addPacketHandler(new SayMessageHandler(HEADER_SAY));
			localClient.addPacketHandler(new QueryMessageHandler(HEADER_QUERY));
			localClient.addPacketHandler(new RegisterMessageHandler(HEADER_REGISTER));
			localClient.addPacketHandler(new LoginMessageHandler(HEADER_LOGIN));
			localClient.addPacketHandler(new PlayersInSightMessageHandler(HEADER_PLAYERS_IN_SIGHT_AROUND));
			localClient.addPacketHandler(new GetPositionInfoMessageHandler(HEADER_GET_POSITION_INFO));
			localClient.addPacketHandler(new BattleBeginsMessageHandler(HEADER_BATTLE_BEGINS));
			localClient.addPacketHandler(new BattleMoveMessageHandler(HEADER_BATTLE_MOVE_ORDER));
			localClient.addPacketHandler(new BattlePickMessageHandler(HEADER_BATTLE_PICK_ORDER));
			localClient.addPacketHandler(new BattlePlaceMessageHandler(HEADER_BATTLE_PLACE_ORDER));
			localClient.addPacketHandler(new SkipTurnMessageHandler(HEADER_BATTLE_SKIP_TURN));
			localClient.addPacketHandler(new GetInventoryMessageHandler(HEADER_GET_INVENTORY));
			localClient.addPacketHandler(new PlacementFinishedMessageHandler(HEADER_PLACEMENT_FINISHED));
            localClient.addPacketHandler(new GetFactionInfoMessageHandler(HEADER_GET_FACTION_INFO));
			localClient.addPacketHandler(new CastSpellMessageHandler(HEADER_SPELL_CAST_ORDER));
			localClient.addPacketHandler(new ServerStatusRequestMessageHandler(HEADER_SERVER_STATUS_REQUEST));
			localClient.addPacketHandler(new BattleOverMessageHandler(HEADER_BATTLE_IS_OVER));
			localClient.addPacketHandler(new InstantMoveMessageHandler(HEADER_INSTANT_MOVE));
			localClient.addPacketHandler(new GetProfileInfoMessageHandler(HEADER_GET_PROFILE_INFO));
			localClient.addPacketHandler(new RequestDuelMessageHandler(HEADER_REQUEST_DUEL));
			localClient.addPacketHandler(new RejectDuelMessageHandler(HEADER_REJECT_DUEL));
			localClient.addPacketHandler(new CancelDuelMessageHandler(HEADER_CANCEL_DUEL));
			localClient.addPacketHandler(new DuelTimeoutMessageHandler(HEADER_DUEL_TIMEOUT));
			localClient.addPacketHandler(new BattleTimerUpdateMessageHandler(HEADER_BATTLE_TIMER_UPDATE));
			localClient.addPacketHandler(new ChannelListMessageHandler(HEADER_CHANNEL_LIST));
			localClient.addPacketHandler(new JoinChannelMessageHandler(HEADER_JOIN_CHANNEL));
			localClient.addPacketHandler(new LeaveChannelMessageHandler(HEADER_LEAVE_CHANNEL));
			localClient.addPacketHandler(new AllChannelsListMessageHandler(HEADER_ALL_CHANNELS_LIST));
			localClient.addPacketHandler(new CastCardSpellMessageHandler(HEADER_CARD_CAST_ORDER));
			localClient.addPacketHandler(new GetItemsMessageHandler(HEADER_GET_ITEMS));
			localClient.addPacketHandler(new GetCharacterInfoMessageHandler(HEADER_GET_CHARACTER_INFO));

			localClient.addPacketHandler(new ReliableMessageHandler(HEADER_RELIABLE));

		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void updateInventory(){
		try {
			localClient.send(new Packet(HEADER_GET_INVENTORY));
		} catch (IncorrectPacketException e) {
			e.printStackTrace();
		}
	}

	public ClientInventory getMainInventory() {
		return mainInventory;
	}

	public void enqueue(Callable callable){
		synchronized (mainThread.queue){
			mainThread.queue.add(callable);
		}
	}

	public boolean isOnDestroy() {
		return onDestroy;
	}

	public LocalClient getLocalClient() {
		return localClient;
	}

	public static void main(String[] args) {
		Program.getInstance().start();
	}
}
