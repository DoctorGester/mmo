package core.graphics.scenes;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import core.board.*;
import core.board.turns.TurnQueue;
import core.board.turns.TurnSmart;
import core.graphics.FloatingText;
import core.graphics.MainFrame;
import core.graphics.MaterialDebugAppState;
import core.graphics.UnitSpatial;
import core.main.CardMaster;
import core.main.inventory.items.SpellCardItem;
import core.ui.BattleController;
import core.ui.BattleState;
import core.ui.UI;
import program.main.Program;
import program.main.Util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class BattleScene extends AbstractScene implements ActionListener {
	private DirectionalLight battleSceneSun;

	private BattleState battleState;
	private TurnQueue turnQueue = new TurnQueue();

	private List<UnitSpatial> unitSpatials;
	private List<FloatingText> floatingTexts;

	private Material materialNone,
					 materialMove,
					 materialStripedRed,
					 materialStripedBlue,
					 materialPossibleMove;

	private Program program;

	private ChaseCamera camera;
	private Spell focusedSpell;
	private SpellCardItem focusedSpellCard;
	private CardSpell focusedCardSpell;
	private float timePassedSinceQuadUpdate;

	private static final float QUAD_UPDATE_PERIOD = 0.1f;

	@Override
	public void setupCamera(ChaseCamera camera) {
		camera.setDefaultDistance(40);
		camera.setMinDistance(20);
		camera.setMaxDistance(60);
		camera.setDragToRotate(true);
		camera.setInvertVerticalAxis(true);
		camera.setSmoothMotion(true);
		camera.setMinVerticalRotation(FastMath.DEG_TO_RAD * 20);
		camera.setMaxVerticalRotation(FastMath.DEG_TO_RAD * 80);
		camera.setTrailingEnabled(false);
		camera.setChasingSensitivity(50f);
		camera.setDownRotateOnCloseViewOnly(false);
		camera.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 50);

		camera.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE),
				new KeyTrigger(KeyInput.KEY_LCONTROL));

		this.camera = camera;
	}

	@Override
	public void setupLight(DirectionalLightShadowRenderer shadowRenderer) {
		if (battleSceneSun == null){
			battleSceneSun = new DirectionalLight();
			battleSceneSun.setColor(ColorRGBA.LightGray);
			battleSceneSun.setDirection(new Vector3f(-0.5f, -3.0f, -0.5f).normalizeLocal());
			root.addLight(battleSceneSun);

			AmbientLight ambientLight = new AmbientLight();
			ambientLight.setColor(ColorRGBA.White);
			root.addLight(ambientLight);
		}
		shadowRenderer.setLight(battleSceneSun);
	}

	@Override
	public void loadScene(SimpleApplication app) {
		unitSpatials = new ArrayList<UnitSpatial>();
		floatingTexts = new LinkedList<FloatingText>();

		program = Program.getInstance();

		Board board = battleState.getBoard();

		Node quads = (Node) root.getChild("quads");

		if (quads == null) {
			quads = new Node("quads");
			root.attachChild(quads);
		} else {
			quads.detachAllChildren();
		}

		float quadSize = Cell.CELL_WIDTH * 0.1f;

		Quad quad = new Quad(quadSize - 0.1f, quadSize - 0.1f);
		Geometry geometry = new Geometry("Quad", quad);
		geometry.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
		geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		Material temp = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		temp.setBoolean("UseMaterialColors", true);
		geometry.setMaterial(temp);

		for (int y = 0; y < board.getHeight(); y++)
			for (int x = 0; x < board.getWidth(); x++) {
				Spatial spatial = geometry.clone();
				spatial.setUserData("x", x);
				spatial.setUserData("y", y);
				spatial.setLocalTranslation(x * quadSize, 0, y * quadSize);

				quads.attachChild(spatial);
			}

		Node center = new Node("center");
		root.attachChild(center);
		center.setLocalTranslation(board.getWidth() * quadSize / 2f, 0,
								  (board.getHeight() - 1) * quadSize / 2f - quadSize / 2f);

		loadMaterials(app.getAssetManager());
		loadFromBoard(board);

		camera.setSpatial(center);
		center.removeControl(camera);
		center.addControl(camera);

		app.getRootNode().attachChild(root);

		loadTerrain(app);
		loadSky(app);
	}

	private void loadSky(SimpleApplication app){
		AssetManager assetManager = app.getAssetManager();

		Texture west = assetManager.loadTexture("res/textures/skybox/checkered/checkered_left.jpg");
		Texture east = assetManager.loadTexture("res/textures/skybox/checkered/checkered_right.jpg");
		Texture north = assetManager.loadTexture("res/textures/skybox/checkered/checkered_front.jpg");
		Texture south = assetManager.loadTexture("res/textures/skybox/checkered/checkered_back.jpg");
		Texture up = assetManager.loadTexture("res/textures/skybox/checkered/checkered_top.jpg");
		Texture down = assetManager.loadTexture("res/textures/skybox/checkered/checkered_top.jpg");

		Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
		root.attachChild(sky);
	}

	private Material createTerrainMaterial(SimpleApplication app){
		float grassScale = 64;
		float dirtScale = 16;
		float rockScale = 128;

		AssetManager assetManager = app.getAssetManager();

		Material material = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(Texture.WrapMode.Repeat);
		material.setTexture("region1ColorMap", grass);
		material.setVector3("region1", new Vector3f(15, 200, grassScale));

		// DIRT texture
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(Texture.WrapMode.Repeat);
		material.setTexture("region2ColorMap", dirt);
		material.setVector3("region2", new Vector3f(0, 20, dirtScale));

		// ROCK texture
		Texture rock = assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
		rock.setWrap(Texture.WrapMode.Repeat);
		material.setTexture("region3ColorMap", rock);
		material.setVector3("region3", new Vector3f(198, 260, rockScale));

		material.setTexture("region4ColorMap", rock);
		material.setVector3("region4", new Vector3f(198, 260, rockScale));

		material.setTexture("slopeColorMap", rock);
		material.setFloat("slopeTileFactor", 32);

		material.setFloat("terrainSize", 513);

		return material;
	}

	private void loadTerrain(SimpleApplication app){
		Texture texture = app.getAssetManager().loadTexture("res/map/map-0-7.png");
		HeightMap heightMap = new ImageBasedHeightMap(texture.getImage());
		heightMap.load();

		TerrainQuad terrainQuad = new TerrainQuad("BattleQuad", 65, 257, heightMap.getHeightMap());

		terrainQuad.setMaterial(createTerrainMaterial(app));
		terrainQuad.setLocalTranslation(new Vector3f(0, -25.7f, 0)); // (256 + 1) * 0.1
		terrainQuad.setShadowMode(RenderQueue.ShadowMode.Receive);
		terrainQuad.setLocalScale(1, 0.1f, 1);

		root.attachChild(terrainQuad);
	}

	@Override
	public void unloadScene(SimpleApplication app) {
		super.unloadScene(app);

		for (UnitSpatial unitSpatial : unitSpatials)
			unitSpatial.getNode().removeFromParent();
	}

	@Override
	public void updateScene(SimpleApplication app, float tpf) {
		turnQueue.update(tpf);
		updateFloatingText(tpf);
		updateHover(app);
		updateSpatials(app, tpf);
	}

	public BattleState getBattleState() {
		return battleState;
	}

	public void setBattleState(BattleState battleState) {
		this.battleState = battleState;
	}

	public void updateFloatingText(float tpf){
		for(Iterator<FloatingText> iterator = floatingTexts.iterator(); iterator.hasNext(); ){
			FloatingText text = iterator.next();
			text.update(tpf);
			if (text.isOnDestroy()){
				text.getNode().removeFromParent();
				iterator.remove();
			}
		}
	}

	public void addFloatingText(final FloatingText text){
		program.getMainFrame().enqueue(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				floatingTexts.add(text);
				root.attachChild(text.getNode());
				return null;
			}
		});
	}

	private int findPlayerNumber(Board board, CardMaster master){
		int num = -1;
		for(CardMaster cm: board.getCardMasters()){
			num++;
			if (cm == master)
				break;
		}
		return num;
	}

	public ColorRGBA getPlayerColor(Board board, CardMaster player){
		return Board.PLAYER_COLORS[findPlayerNumber(board, player)];
	}

	private void loadMaterials(AssetManager assetManager){
		ColorRGBA base = ColorRGBA.LightGray;//.mult(0.7f);

		materialNone = new Material(assetManager, "res/shaders/ColoredCell.j3md");
		materialNone.setBoolean("UseMaterialColors", true);
		materialNone.setColor("Diffuse", base);
		materialNone.setColor("Specular", ColorRGBA.White);

		materialMove = materialNone.clone();
		materialMove.setColor("Diffuse", new ColorRGBA(0.6f, 0.88f, 0.5f, 1.0f));

		materialPossibleMove = materialNone.clone();
		materialPossibleMove.setColor("Diffuse", new ColorRGBA(0.42f, 0.63f, 0.36f, 1.0f));

		materialStripedRed = new Material(assetManager, "res/shaders/ColoredCell.j3md");
		materialStripedRed.setColor("Specular", base);
		materialStripedRed.setColor("Front", ColorRGBA.Red.mult(0.7f));
		materialStripedRed.setColor("Back", base);
		materialStripedRed.setTexture("DiffuseMap", assetManager.loadTexture("res/textures/stripe.png"));

		materialStripedBlue = new Material(assetManager, "res/shaders/ColoredCell.j3md");
		materialStripedBlue.setColor("Specular", base);
		materialStripedBlue.setColor("Front", ColorRGBA.Blue.mult(0.7f));
		materialStripedBlue.setColor("Back", base);
		materialStripedBlue.setTexture("DiffuseMap", assetManager.loadTexture("res/textures/stripe.png"));
	}

	public UnitSpatial getSpatialByUnit(Unit unit){
		for (UnitSpatial unitSpatial: unitSpatials)
			if (unitSpatial.getUnit() == unit)
				return unitSpatial;
		return null;
	}

	public List<UnitSpatial> getUnitSpatials() {
		return unitSpatials;
	}

	private static boolean pointInsideArea(Cell point, Rectangle area){
		int x = point.getX(),
				y = point.getY();
		return (x >= area.x && x <= area.x + area.width &&
				y >= area.y && y < area.y + area.height);
	}

	public Cell getCellAtCursor(Board board, Vector2f clickPosition){
		Camera cam = program.getMainFrame().getCamera();

		Node quads = (Node) root.getChild("quads");

		Vector3f worldPosition = cam.getWorldCoordinates(clickPosition, 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(clickPosition, 1f).subtractLocal(worldPosition).normalizeLocal();

		Ray ray = new Ray(worldPosition, dir);
		CollisionResults results = new CollisionResults();

		// First check collision with all board units TODO fix this
		/*List<UnitSpatial> unitSpatials = getUnitSpatials();
		for(UnitSpatial unitSpatial: unitSpatials){
			if (unitSpatial.getSpatial().getWorldBound().collideWith(ray, results) != 0){
				return unitSpatial.getUnit().getPosition();
			}
		}*/

		if (quads == null)
			return null;

		quads.collideWith(ray, results);

		if (results.size() == 0)
			return null;

		Geometry clicked = results.getCollision(0).getGeometry();
		int x = clicked.getUserData("x"),
				y = clicked.getUserData("y");

		return board.getCellChecked(x, y);
	}

	private void defaultCursor(){
		if (battleState.getSpellToCast() == null)
			program.getMainFrame().setCursor(UI.CURSOR_DEFAULT);
	}

	private void updateHover(Application app){
		if (timePassedSinceQuadUpdate < QUAD_UPDATE_PERIOD)
			return;

		MainFrame frame = program.getMainFrame();
		Board board = battleState.getBoard();
		Cell hover = getCellAtCursor(board, app.getInputManager().getCursorPosition());
		Unit unit = battleState.getSelectedUnit();
		if (unit == null || hover == null || battleState.getSpellToCast() != null){
			defaultCursor();
			return;
		}

		CardMaster main = program.getMainPlayer();
		if (unit.getState() != Unit.STATE_WAIT || unit.getOwner() != main
				|| (main != board.getCurrentTurningPlayer() && board.getState() != Board.STATE_WAIT_FOR_PLACEMENT)){
			defaultCursor();
			return;
		}

		if (board.getState() == Board.STATE_WAIT_FOR_ORDER){
			if (hover.getContentsType() == Cell.CONTENTS_UNIT){
				boolean canAttack = (Boolean) unit.callEvent(Unit.SCRIPT_EVENT_CHECK_ATTACK, hover.getUnit(), unit.getPosition());
				if (canAttack){
					frame.setCursor(UI.CURSOR_ATTACK);
					return;
				}
			} else if (hover.getContentsType() == Cell.CONTENTS_EMPTY){
				if (unit.canMoveTo(hover)){
					frame.setCursor(UI.CURSOR_MOVE);
					return;
				}
			}
		} else if (board.getState() == Board.STATE_WAIT_FOR_PLACEMENT){
			if (hover.getContentsType() == Cell.CONTENTS_EMPTY){
				if (pointInsideArea(hover, board.getPlacementArea()[unit.getOwner().getBattleId()])){
					frame.setCursor(UI.CURSOR_MOVE);
					return;
				}
			}
		}

		defaultCursor();
	}

	private static boolean checkTargets(Spell spell, Cell cell){
		SpellData spellData = spell.getSpellData();
		Unit unit = spell.getCaster();

		boolean unitTarget = (spellData.targetAllowed(SpellTarget.UNIT) &&
				cell.getContentsType() == Cell.CONTENTS_UNIT) &&
				!(!spellData.targetAllowed(SpellTarget.SELF) &&
						cell == unit.getPosition());

		boolean selfTarget = spellData.targetAllowed(SpellTarget.SELF) &&
				cell == unit.getPosition();

		boolean groundTarget = spellData.targetAllowed(SpellTarget.GROUND) &&
				cell.getContentsType() == Cell.CONTENTS_EMPTY;

		return (unitTarget || selfTarget || groundTarget);
	}

	private void updateSpatials(Application app, float tpf){
		Unit selectedUnit = battleState.getSelectedUnit();

		for(UnitSpatial unitSpatial: unitSpatials){
			Unit unit = unitSpatial.getUnit();
			Spatial spatial = unitSpatial.getSpatial();
			Node node = unitSpatial.getNode();

			// Updating node position
			float quadSizeHalf = Cell.CELL_WIDTH * 0.1f / 2f;

			node.setLocalTranslation(unit.getRealPositionX() * 0.1f + quadSizeHalf, 0,
					unit.getRealPositionY() * 0.1f - quadSizeHalf);

			// Updating unit facing
			Quaternion q = new Quaternion();
			Vector2f pos = unit.getFacing();
			q.lookAt(new Vector3f(pos.x, 0, pos.y), Vector3f.UNIT_Y);
			spatial.setLocalRotation(q);

			// Updating unit animation
			AnimChannel channel = unitSpatial.getAnimChannel();
			if (channel != null){
				String currentAnimation = channel.getAnimationName(),
						unitAnimation = unit.getAnimationName();

				float speed = unit.getAnimationSpeed();

				if (currentAnimation == null)
					currentAnimation = "";

				if (!unitAnimation.equals(currentAnimation) && channel.getControl().getAnim(unitAnimation) != null){
					channel.setAnim(unitAnimation, 0.25f);
					channel.setSpeed(speed);
					if (unit.isAnimationLooped())
						channel.setLoopMode(LoopMode.Loop);
					else
						channel.setLoopMode(LoopMode.DontLoop);
				}
			}

			// Updating hp bar
			unitSpatial.updateBars();
			unitSpatial.setSelected(unit == selectedUnit);
		}

		timePassedSinceQuadUpdate += tpf;
		if (timePassedSinceQuadUpdate < QUAD_UPDATE_PERIOD)
			return;
		else
			timePassedSinceQuadUpdate = 0f;

		// Updating scene
		Node quads = (Node) root.getChild("quads");

		if (quads == null)
			return;

		List<Spatial> spatialList = quads.getChildren();

		if (spatialList == null)
			return;

		Board board = battleState.getBoard();
		Cell hover = getCellAtCursor(board, app.getInputManager().getCursorPosition());
		Spell toCast = battleState.getSpellToCast();

		for(Spatial spatial: spatialList){
			int x = spatial.getUserData("x"),
				y = spatial.getUserData("y");

			Material toSet = materialNone;
			Cell thisCell = board.getCell(x, y);

			if (hover != null && toCast == null && selectedUnit != null
					&& selectedUnit.getState() != Unit.STATE_DEAD
					&& hover.getContentsType() == Cell.CONTENTS_UNIT){
				boolean canAttack = (Boolean) selectedUnit.callEvent(Unit.SCRIPT_EVENT_CHECK_ATTACK, hover.getUnit(), selectedUnit.getPosition()) &&
											  !selectedUnit.controlApplied(ControlType.STUN) && !selectedUnit.controlApplied(ControlType.DISARM);
				if (canAttack){
					boolean inAOE = (Boolean) selectedUnit.callEvent(Unit.SCRIPT_EVENT_CHECK_AOE, hover.getUnit(), thisCell);
					if (inAOE)
						toSet = materialStripedRed;
				}
			}

			Spell aoeSpell = toCast == null ? focusedSpell : toCast;

			if (aoeSpell != null && selectedUnit != null
				&& selectedUnit.getState() != Unit.STATE_DEAD
				&& !aoeSpell.getSpellData().onlyAllowed(SpellTarget.SELF)
				&& (Boolean) aoeSpell.callEvent(Spell.SCRIPT_EVENT_CHECK, thisCell)){

				if (checkTargets(aoeSpell, thisCell))
					toSet = materialStripedBlue;
			}

			if (focusedSpellCard != null && focusedCardSpell.checkCell(thisCell))
				toSet = materialStripedBlue;

			Cell aoePointer = null;
			Spell spell = null;
			if (toCast != null && focusedSpell == null && focusedSpellCard == null) {
				aoePointer = hover;
				spell = toCast;
			}
			if (toCast == null && focusedSpell != null && focusedSpellCard == null && selectedUnit != null
					&& focusedSpell.getSpellData().onlyAllowed(SpellTarget.SELF)) {
				aoePointer = selectedUnit.getPosition();
				spell = focusedSpell;
			}

			if (aoePointer != null
					&& spell.checkAOE(aoePointer, thisCell)
					&& (Boolean) spell.callEvent(Spell.SCRIPT_EVENT_CHECK, aoePointer)
					&& checkTargets(spell, aoePointer))
				toSet = materialStripedRed;

			if (board.getState() == Board.STATE_WAIT_FOR_PLACEMENT){
				if (toSet == materialNone && selectedUnit != null && selectedUnit.getOwner() == program.getMainPlayer()){
					// Make quad green when it enters the area in which placement is allowed
					// and it's not occupied
					if (thisCell.getContentsType() == Cell.CONTENTS_EMPTY &&
							pointInsideArea(thisCell, board.getPlacementArea()[selectedUnit.getOwner().getBattleId()]))
						toSet = materialMove;
				}
			} else {
				if (toSet == materialNone && selectedUnit != null){
					if (selectedUnit.getState() == Unit.STATE_WAIT && selectedUnit.canMoveTo(thisCell))
						toSet = materialMove;
					else if (selectedUnit.canPossiblyMoveTo(thisCell))
						toSet = materialPossibleMove;
				}
			}

			if (thisCell.getContentsType() == Cell.CONTENTS_UNIT){
				UnitSpatial unitSpatial = getSpatialByUnit(thisCell.getUnit());
				if (unitSpatial != null)
					unitSpatial.setSelectionVisible(toSet != materialStripedBlue && toSet != materialStripedRed);
			}

			spatial.setMaterial(toSet);
		}
	}

	public TurnQueue getTurnQueue() {
		return turnQueue;
	}

	public void loadFromBoard(final Board board){
		final Application frame = Program.getInstance().getMainFrame();

		frame.enqueue(new Callable<Void>() {
			public Void call() throws Exception {
				MaterialDebugAppState state = new MaterialDebugAppState();

				List<Unit> units = board.getUnits();
				for (Unit unit : units) {
					UnitSpatial unitSpatial = new UnitSpatial(unit.getUnitData().getModel(true), unit);
					unitSpatial.createBars(frame.getAssetManager());
					unitSpatial.debug(state);
					unitSpatials.add(unitSpatial);

					root.attachChild(unitSpatial.getNode());
				}

				frame.getStateManager().attach(state);
				return null;
			}
		});
	}

	public void setFocusedSpell(Spell focusedSpell) {
		this.focusedSpell = focusedSpell;
	}

	public void setFocusedSpellCard(SpellCardItem item){
		if (item != focusedSpellCard && item != null){
			CardSpellData spellData = Program.getInstance().getCardSpellDataById(item.getSpellId());
			focusedCardSpell = new CardSpell(spellData, program.getMainPlayer(), battleState.getBoard());
		}
		this.focusedSpellCard = item;
	}

	public void onAction(String name, boolean pressed, float tpf) {
		BattleController controller = program.getBattleController();
		MainFrame mainFrame = program.getMainFrame();

		Board board = battleState.getBoard();
		Unit selectedUnit = battleState.getSelectedUnit();
		boolean isCastMode = battleState.isCastMode();

		if (name.equals("leftClick") && pressed){
			Cell clicked = getCellAtCursor(board, mainFrame.getInputManager().getCursorPosition());

			if (isCastMode && clicked == null)
				return;

			if (isCastMode && selectedUnit != null){
				controller.battleCast(selectedUnit, clicked, battleState.getSpellToCastNumber());
				controller.endCast(battleState);
			} else {
				if (clicked != null && clicked.getContentsType() == Cell.CONTENTS_UNIT){

					// We don't have to deselect our unit every time player
					// selected wrong one
					if (clicked.getUnit().getState() != Unit.STATE_DEAD)
						battleState.setSelectedUnit(clicked.getUnit());
				} else {
					battleState.setSelectedUnit(null);
				}
			}

			controller.updateUnitUI(battleState);
		}

		if (isCastMode && name.equals("rightClick") && !pressed){
			controller.endCast(battleState);
			return;
		}

		if (isCastMode)
			return;

		if (name.equals("space") && !pressed){
			controller.battleSkipTurn();
		}

		if (name.equals("rightClick") && !pressed && selectedUnit != null){
			Cell clicked = getCellAtCursor(board, mainFrame.getInputManager().getCursorPosition());

			if (clicked == null)
				return;

			if (clicked.getContentsType() == Cell.CONTENTS_UNIT)
				controller.battleAttackUnit(board, selectedUnit, clicked.getUnit());
			else
				controller.battleMoveUnit(board, selectedUnit, clicked);
		}
	}
}
