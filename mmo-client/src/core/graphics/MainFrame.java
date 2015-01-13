package core.graphics;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.font.BitmapFont;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import core.graphics.scenes.*;
import core.ui.ChatUIState;
import core.ui.UI;
import core.ui.battle.*;
import core.ui.map.CardMasterProfileUIState;
import core.ui.map.CardbookUIState;
import core.ui.map.RequestsUIState;
import core.ui.map.MapUIState;
import core.ui.menu.MenuUIState;
import core.ui.menu.ServerSelectionUIState;
import program.main.Program;
import program.main.data.TextLoader;
import tonegod.gui.core.Screen;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.*;
import java.util.logging.Formatter;

public class MainFrame extends SimpleApplication {
	private Screen guiScreen;
	private ChaseCamera chaseCamera;
	private DirectionalLightShadowRenderer shadowRenderer;

	private Scene currentScene;

	private final Map<String, AppState> uiStates = new HashMap<String, AppState>();
	private final Map<String, Scene> scenes = new HashMap<String, Scene>();
	private final Map<String, JmeCursor> cursors = new HashMap<String, JmeCursor>();

	private final List<SpecialEffect> effects = new ArrayList<SpecialEffect>();

	private String currentCursor = "";

	private BitmapFont defaultFont, outlinedFont;

	public MainFrame() {
		// TODO don't forget to remove this
		Logger.getLogger("").setLevel(Level.ALL);
		/*for (Handler handler: Logger.getLogger("").getHandlers())
			if (handler instanceof ConsoleHandler){
				handler.setFormatter(new Formatter() {
					@Override
					public String format(LogRecord record) {
						final String format = "[%s] (%s) %s.%s: %s\n";
						String time = new SimpleDateFormat("HH:mm:ss.S").format(new Date(record.getMillis()));

						return String.format(format, record.getLevel(), time, record.getLoggerName(), record.getSourceMethodName(), record.getMessage());
					}
				});
			}*/
		
		AppSettings settings = new AppSettings(true);
		settings.setResolution(1093, 614);
		settings.setFrameRate(60);
		settings.setTitle("MMO");
		//settings.setSamples(4);
		//settings.setVSync(true);

		//settings.setResolution(1440, 900);
		//settings.setFullscreen(true);

		setSettings(settings);  
		setDisplayStatView(false);
		setShowSettings(false);

		start();
	}

	public BitmapFont getDefaultFont(){
		return defaultFont;
	}

	public BitmapFont getOutlinedFont() {
		return outlinedFont;
	}

	// TODO
	public BitmapFont getFont(String name){
		return null;
	}

	public Screen getGuiScreen(){
		return guiScreen;
	}

	public void addEffect(SpecialEffect effect){
		synchronized (effects){
			effects.add(effect);
		}
	}

	public Scene getCurrentScene() {
		return currentScene;
	}

	private void addCursor(String name){
		JmeCursor cursor = (JmeCursor) assetManager.loadAsset("res/other/" + name);
		cursors.put(name, cursor);
	}

	public void setCursor(String name){
		if (name.equals(currentCursor) || cursors.get(name) == null)
			return;
		inputManager.setMouseCursor(cursors.get(name));
		currentCursor = name;
	}

	private void initData(){
		addCursor(UI.CURSOR_DEFAULT);
		addCursor(UI.CURSOR_ATTACK);
		addCursor(UI.CURSOR_MOVE);
		addCursor(UI.CURSOR_CAST);

		setCursor(UI.CURSOR_DEFAULT);

		addUIStateToList(UI.STATE_SERVER_SELECTION, new ServerSelectionUIState(this));
		addUIStateToList(UI.STATE_MAIN_MENU, new MenuUIState(this));
		addUIStateToList(UI.STATE_MAP_MAIN, new MapUIState(this));
		addUIStateToList(UI.STATE_BATTLE, new BattleUIState(this));
		addUIStateToList(UI.STATE_BATTLE_PICK_INTERFACE, new BattlePickUIState(this));
		addUIStateToList(UI.STATE_BATTLE_PLACEMENT_INTERFACE, new BattlePlacementUIState(this));
		addUIStateToList(UI.STATE_BATTLE_OVER, new BattleOverUIState(this));
		addUIStateToList(UI.STATE_CARDBOOK, new CardbookUIState(this));
		addUIStateToList(UI.STATE_PROFILE, new CardMasterProfileUIState(this));
		addUIStateToList(UI.STATE_REQUESTS, new RequestsUIState());
		addUIStateToList(UI.STATE_CHAT, new ChatUIState(this));
		addUIStateToList(UI.STATE_BATTLE_LOG, new BattleLogUIState(this));
		addUIStateToList(UI.STATE_SPELL_SELECTOR, new SpellSelectorUIState(this));

		setUIState(UI.STATE_SERVER_SELECTION);

		addScene(Scenes.MAIN_MAP, new MapScene());
		addScene(Scenes.BATTLE, new BattleScene());
		addScene(Scenes.MENU, new MenuScene());
	}

	public void simpleInitApp() {
		getAssetManager().registerLocator("", FileLocator.class);
		//getAssetManager().registerLocator("tonegod-gui", FileLocator.class);
		getAssetManager().registerLoader(TextLoader.class, "txt", "anims");

		setPauseOnLostFocus(false);

		guiScreen = new Screen(this, "style/style_map.gui.xml");
		guiNode.addControl(guiScreen);

		flyCam.setEnabled(false);
		inputManager.setCursorVisible(true);

		cam.setFrustumFar(1000);

		chaseCamera = new ChaseCamera(cam, inputManager);

		// Init main shadow render
		shadowRenderer = new DirectionalLightShadowRenderer(assetManager, 1024, 3);
		viewPort.addProcessor(shadowRenderer);

		defaultFont = assetManager.loadFont("res/other/segoe.fnt");
		outlinedFont = assetManager.loadFont("res/other/segoe_outlined.fnt");

		// Glow
		FilterPostProcessor processor = new FilterPostProcessor(assetManager);
		BloomFilter filter = new BloomFilter(BloomFilter.GlowMode.Objects);
		processor.addFilter(filter);
		filter.setDownSamplingFactor(2.0f);
		filter.setBloomIntensity(4);
		viewPort.addProcessor(processor);

		ScreenshotAppState screenShotState = new ScreenshotAppState();
		inputManager.addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_F6));
		inputManager.addListener(screenShotState, "ScreenShot");

		stateManager.attach(screenShotState);

		initData();

		Program.getInstance().endGraphicsInit();
	}

	public void addScene(String name, Scene scene){
		scenes.put(name, scene);
	}

	public <T extends Scene> T getScene(String name, Class<T> type){
		if (type.isInstance(scenes.get(name)))
			return type.cast(scenes.get(name));
		return null;
	}

	public void setScene(final String name){
		enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				synchronized (scenes){
					Scene scene = scenes.get(name);
					if (scene == null)
						return null;

					if (currentScene != null)
						currentScene.unloadScene(MainFrame.this);

					scene.setupCamera(chaseCamera);
					scene.setupLight(shadowRenderer);
					scene.setupInput(inputManager);
					scene.loadScene(MainFrame.this);

					currentScene = scene;

					return null;
				}
			}
		});
	}

	public void addUIStateToList(String name, AbstractAppState state){
		uiStates.put(name, state);
	}

	public AppState getUIState(String name){
		return uiStates.get(name);
	}

	public <T extends AbstractAppState> T getUIState(String name, Class<T> type){
		if (type.isInstance(uiStates.get(name)))
			return type.cast(uiStates.get(name));
		return null;
	}

	public void removeUIState(final String name){
		enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				stateManager.detach(getUIState(name));
				return null;
			}
		});
	}

	public void addUIState(final String name){
		enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				stateManager.attach(getUIState(name));
				return null;
			}
		});
	}

	public boolean hasUIState(String name){
		return stateManager.hasState(getUIState(name));
	}

	public synchronized void setUIState(final String name){
		enqueue(new Callable<Object>() {
			public Object call() throws Exception {
				AppState newState = getUIState(name);

				for (AppState state: uiStates.values())
					if (stateManager.hasState(state))
						stateManager.detach(state);

				stateManager.attach(newState);
				return null;
			}
		});
	}

	public void simpleUpdate(float tpf){
		synchronized (scenes){
			if (currentScene != null)
				currentScene.updateScene(this, tpf);
		}

		synchronized (effects){
			for(Iterator<SpecialEffect> iterator = effects.iterator(); iterator.hasNext(); ){
				SpecialEffect effect = iterator.next();

				if (effect.update(tpf)){
					iterator.remove();
				}
			}
		}
	}

	public void destroy(){
		super.destroy();
		Program.getInstance().setOnDestroy(true);
	}
}