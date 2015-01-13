package core.graphics.scenes;

import com.jme3.animation.AnimChannel;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import core.graphics.CardMasterSpatial;
import core.graphics.TerrainPager;
import core.main.*;
import jme3tools.optimize.GeometryBatchFactory;
import program.main.Program;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author doc
 */
public class MapScene extends AbstractScene{
	private boolean created = false;
	private DirectionalLight mapSceneSun;
	private TerrainPager terrain;

	private Node visible = new Node(),
				 invisible = new Node();

	private ChaseCamera camera;
	private boolean cameraAttached = false;

	private Set<CardMasterSpatial> visibleCardMasterSpatials = new CopyOnWriteArraySet<CardMasterSpatial>(),
								   invisibleCardMasterSpatials = new CopyOnWriteArraySet<CardMasterSpatial>();

	public TerrainPager getTerrain() {
		return terrain;
	}

	private void createScene(SimpleApplication app){
		AssetManager assetManager = app.getAssetManager();

		Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
		Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
		Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
		Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
		Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
		Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

		Spatial sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down);
		root.attachChild(sky);

		terrain = createTerrain(app);
		root.attachChild(terrain);
		root.attachChild(visible);

		//loadObstacles(app);

		System.out.println(Thread.currentThread().getName());
	}

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
		if (mapSceneSun == null){
			mapSceneSun = new DirectionalLight();
			mapSceneSun.setColor(ColorRGBA.White);
			mapSceneSun.setDirection(new Vector3f(-0.5f,-5f,-0.5f).normalizeLocal());
			root.addLight(mapSceneSun);
		}
		shadowRenderer.setLight(mapSceneSun);
	}

	@Override
	public void loadScene(SimpleApplication app) {
		cameraAttached = false;

		if (!created){
			createScene(app);
			created = true;
		}

		app.getRootNode().attachChild(root);
	}

	public CardMaster getCardMasterAtCursor(Vector2f clickPosition){
		Camera cam = Program.getInstance().getMainFrame().getCamera();

		Vector3f worldPosition = cam.getWorldCoordinates(clickPosition, 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(clickPosition, 1f).subtractLocal(worldPosition).normalizeLocal();

		Ray ray = new Ray(worldPosition, dir);
		CollisionResults results = new CollisionResults();

		visible.collideWith(ray, results);

		for (CollisionResult collision: results)
			for (CardMasterSpatial spatial: visibleCardMasterSpatials)
				if (spatial.getGeometry() == collision.getGeometry())
					return spatial.getCardMaster();

		return null;
	}

	private void updateSpatialStates(){
		for(CardMasterSpatial cardMasterSpatial: visibleCardMasterSpatials){
			CardMaster cardMaster = cardMasterSpatial.getCardMaster();
			Node node = cardMasterSpatial.getNode();
			Spatial spatial = cardMasterSpatial.getSpatial();

			// Update position
			Hero hero = cardMaster.getHero();
			Vector2f xzPosition = new Vector2f(hero.getX() * 0.1f, hero.getY() * 0.1f);
			Float height = terrain.getHeight(xzPosition);
			if (height.equals(Float.NaN))
				height = node.getLocalTranslation().z;

			node.setLocalTranslation(xzPosition.x, height, xzPosition.y); // Y and Z are swapped

			// Update rotation
			Quaternion q = new Quaternion();

			if (hero.getPath() != null){
				Vector2f pos = hero.getFacing();

				q.lookAt(new Vector3f(pos.x, 0, pos.y), Vector3f.UNIT_Y);
			}

			spatial.setLocalRotation(q);

			// Update name
			for(CardMaster player: Program.getInstance().getVisiblePlayers())
				if (player == cardMaster && !player.getName().equals(cardMasterSpatial.getName()))
					cardMasterSpatial.setName(player.getName());

			// Update animation
			AnimChannel channel = cardMasterSpatial.getAnimChannel();
			String currentAnimation = channel.getAnimationName();

			if (currentAnimation == null)
				currentAnimation = "";

			switch (hero.getOrder()){
				case Hero.ORDER_STOP:{
					if (!currentAnimation.equals("stand"))
						channel.setAnim("stand", 0.25f);
					break;
				}
				case Hero.ORDER_MOVE:{
					if (!currentAnimation.equals("walk"))
						channel.setAnim("walk", 0.25f);
					break;
				}
			}
		}
	}

	public CardMasterSpatial getCardMasterSpatialByCardMaster(CardMaster cardMaster){
		for(CardMasterSpatial cardMasterSpatial: visibleCardMasterSpatials){
			if (cardMasterSpatial.getCardMaster() == cardMaster)
				return cardMasterSpatial;
		}
		return null;
	}

	@Override
	public void updateScene(SimpleApplication app, float tpf) {
		Program program = Program.getInstance();

		if (program.getMainPlayer() == null)
			return;

		Hero hero = program.getMainPlayer().getHero();
		Vector3f heroPos = new Vector3f(hero.getX() * 0.1f, 0, hero.getY() * 0.1f);
		terrain.update(heroPos);

		if (!cameraAttached && camera != null){
			CardMasterSpatial main = getCardMasterSpatialByCardMaster(program.getMainPlayer());
			if (main != null){
				camera.setSpatial(main.getNode());
				main.getNode().removeControl(camera);
				main.getNode().addControl(camera);
				cameraAttached = true;
			}
		}

		updateSpatialStates();

		// Adding previously invisible spatials to root node
		for (CardMasterSpatial spatial: invisibleCardMasterSpatials){

			CardMaster player = spatial.getCardMaster();

			if (program.getVisiblePlayers().contains(player) && player.isInitialized()){
				invisibleCardMasterSpatials.remove(spatial);
				visibleCardMasterSpatials.add(spatial);
				visible.attachChild(spatial.getNode());
			}

		}

		// Adding recently visible spatials to invisible node
		for (CardMasterSpatial spatial: visibleCardMasterSpatials){

			CardMaster player = spatial.getCardMaster();

			if (program.getInvisiblePlayers().contains(player) || !player.isInitialized()){
				visibleCardMasterSpatials.remove(spatial);
				invisibleCardMasterSpatials.add(spatial);
				invisible.attachChild(spatial.getNode());
			}

		}
	}

	public void addCardMasterSpatial(CardMasterSpatial spatial){
		invisibleCardMasterSpatials.add(spatial);
		invisible.attachChild(spatial.getNode());
	}

	private TerrainPager createTerrain(final SimpleApplication app){
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

		TerrainPager terrain = new TerrainPager(app, 257, 65);
		terrain.setMaterial(material);
		terrain.setLocalScale(1, 0.1f, 1);

		return terrain;

		/*float grassScale = 64;
		float dirtScale = 16;
		float rockScale = 128;

		AssetManager assetManager = app.getAssetManager();
		
		Material mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

		// Parameters to material:
		// regionXColorMap: X = 1..4 the texture that should be appliad to state X
		// regionX: a Vector3f containing the following information:
		//      regionX.x: the start height of the region
		//      regionX.y: the end height of the region
		//      regionX.z: the texture scale for the region
		//  it might not be the most elegant way for storing these 3 values, but it packs the data nicely :)
		// slopeColorMap: the texture to be used for cliffs, and steep mountain sites
		// slopeTileFactor: the texture scale for slopes
		// terrainSize: the total size of the terrain (used for scaling the texture)
		// GRASS texture
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(Texture.WrapMode.Repeat);
		mat_terrain.setTexture("region1ColorMap", grass);
		mat_terrain.setVector3("region1", new Vector3f(15, 200, grassScale));

		// DIRT texture
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(Texture.WrapMode.Repeat);
		mat_terrain.setTexture("region2ColorMap", dirt);
		mat_terrain.setVector3("region2", new Vector3f(0, 20, dirtScale));

		// ROCK texture
		Texture rock = assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
		rock.setWrap(Texture.WrapMode.Repeat);
		mat_terrain.setTexture("region3ColorMap", rock);
		mat_terrain.setVector3("region3", new Vector3f(198, 260, rockScale));

		mat_terrain.setTexture("region4ColorMap", rock);
		mat_terrain.setVector3("region4", new Vector3f(198, 260, rockScale));

		mat_terrain.setTexture("slopeColorMap", rock);
		mat_terrain.setFloat("slopeTileFactor", 32);

		mat_terrain.setFloat("terrainSize", 513);

		FractalSum base = new FractalSum();
		base.setRoughness(0.7f);
		base.setFrequency(1.0f);
		base.setAmplitude(1.0f);
		base.setLacunarity(2.12f);
		base.setOctaves(8);
		base.setScale(0.02125f);
		base.addModulator(new NoiseModulator() {

			@Override
			public float value(float... in) {
				return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
			}
		});

		FilteredBasis ground = new FilteredBasis(base);

		PerturbFilter perturb = new PerturbFilter();
		perturb.setMagnitude(0.119f);

		OptimizedErode therm = new OptimizedErode();
		therm.setRadius(5);
		therm.setTalus(0.011f);

		SmoothFilter smooth = new SmoothFilter();
		smooth.setRadius(1);
		smooth.setEffect(0.7f);

		IterativeFilter iterate = new IterativeFilter();
		iterate.addPreFilter(perturb);
		iterate.addPostFilter(smooth);
		iterate.setFilter(therm);
		iterate.setIterations(1);

		ground.addPreFilter(iterate);

		TerrainGrid terrain = new TerrainGrid("terrain", 65, 513, new FractalTileLoader(ground, 256f));*/
		/*TerrainGrid terrain = new TerrainGrid("terrain", 65, 513, new ImageTileLoader(assetManager, new Namer() {
			@Override
			public String getName(int x, int y) {
				return "res/map/map-" + y + "-" + x + ".png";
			}
		}));*/

		/*terrain.setMaterial(mat_terrain);
		terrain.setLocalTranslation(0, 0, 0);
		terrain.setLocalScale(2f, 1f, 2f);

		TerrainLodControl control = new TerrainGridLodControl(terrain, app.getCamera());
		control.setLodCalculator(new PerspectiveLodCalculator(app.getCamera(), 0.5f)); // patch size, and a multiplier
		terrain.addControl(control);*/

		/*
		
		
		AssetManager assetManager = app.getAssetManager();
		// First, we load up our textures and the heightmap texture for the terrain

		// TERRAIN TEXTURE material
		final Material matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
		matTerrain.setBoolean("useTriPlanarMapping", false);
		matTerrain.setFloat("Shininess", 0.0f);

		// ALPHA map (for splat textures)
		matTerrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

		// HEIGHTMAP image (for the terrain heightmap)
		Texture heightMapImage = assetManager.loadTexture("res/textures/terrain.png");

		// GRASS texture
		Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(Texture.WrapMode.Repeat);
		matTerrain.setTexture("DiffuseMap", grass);
		matTerrain.setFloat("DiffuseMap_0_scale", 64);

		// DIRT texture
		Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(Texture.WrapMode.Repeat);
		matTerrain.setTexture("DiffuseMap_1", dirt);
		matTerrain.setFloat("DiffuseMap_1_scale", 16);

		// ROCK texture
		Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(Texture.WrapMode.Repeat);
		matTerrain.setTexture("DiffuseMap_2", rock);
		matTerrain.setFloat("DiffuseMap_2_scale", 128);

		Texture normalMap0 = assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg");
		normalMap0.setWrap(Texture.WrapMode.Repeat);
		Texture normalMap1 = assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png");
		normalMap1.setWrap(Texture.WrapMode.Repeat);
		Texture normalMap2 = assetManager.loadTexture("Textures/Terrain/splat/road_normal.png");
		normalMap2.setWrap(Texture.WrapMode.Repeat);
		//matTerrain.setTexture("NormalMap", normalMap0);
		matTerrain.setTexture("NormalMap", normalMap0);
		matTerrain.setTexture("NormalMap_1", normalMap1);
		matTerrain.setTexture("NormalMap_2", normalMap2);

		// WIREFRAME material
		Material matWire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		matWire.getAdditionalRenderState().setWireframe(true);
		matWire.setColor("Color", ColorRGBA.Green);

		// CREATE HEIGHTMAP
		AbstractHeightMap heightmap = null;
		try {
			heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

			//heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
			heightmap.load();
			heightmap.smooth(0.8f);

			final AbstractHeightMap finalHeightmap = heightmap;
			TerrainGrid grid = new TerrainGrid("terrain", 65, 513, new TerrainGridTileLoader() {
				private int patchSize;
				private int quadSize;

				@Override
				public TerrainQuad getTerrainQuadAt(Vector3f location) {*/
					/*
					 * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
					 * terrain will be 513x513. It uses the heightmap we created to generate the height values.
					 */
					/*TerrainQuad terrainQuad = new TerrainQuad("terrain", patchSize, quadSize, finalHeightmap.getHeightMap());

					return terrainQuad;
				}

				@Override
				public void setPatchSize(int patchSize) {
					patchSize = patchSize;
				}

				@Override
				public void setQuadSize(int quadSize) {
					quadSize = quadSize;
				}

				public void write(JmeExporter ex) throws IOException {}
				public void read(JmeImporter im) throws IOException {}
			});

			TerrainLodControl control = new TerrainLodControl(grid, app.getCamera());
			//control.setLodCalculator(new DistanceLodCalculator(65, 2.7f)); // patch size, and a multiplier
			control.setLodCalculator(new PerspectiveLodCalculator(app.getCamera(), 0.5f));
			grid.addControl(control);
			grid.setMaterial(matTerrain);
			grid.setLocalScale(2f, 0.5f, 2f);
			grid.setShadowMode(RenderQueue.ShadowMode.Receive);

			return grid;
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		//return terrain;
	}

	private Spatial createObstacle(float x, float z, Material treeMaterial){
		float y = 1.5f + FastMath.nextRandomFloat() * 3f;
		Box b = new Box(0.75f, y, 0.75f);

		Geometry geom = new Geometry("Tree", b);

		geom.setMaterial(treeMaterial);
		geom.setLocalTranslation(x, terrain.getHeight(new Vector2f(x, z)) + y, z);
		return geom;
	}

	public void loadObstacles(Application app){
		PathingMap pathingMap = Program.getInstance().map;
		Node trees = new Node("trees");
		Material treeMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		treeMaterial.setBoolean("UseMaterialColors", true);
		treeMaterial.setColor("Ambient", ColorRGBA.Red);
		treeMaterial.setColor("Diffuse", ColorRGBA.Red);
		treeMaterial.setColor("Specular", ColorRGBA.White);
		treeMaterial.setFloat("Shininess", 12);

		for(int y = 0; y < pathingMap.getHeight(); y++)
			for(int x = 0; x < pathingMap.getWidth(); x++){
				boolean pathable = pathingMap.isPointPathable(x, y);

				if (!pathable)
					trees.attachChild(createObstacle(x * PathingMap.CELL_SIZE * 0.1f, y * PathingMap.CELL_SIZE * 0.1f, treeMaterial));
			}

		Spatial treeBatch = GeometryBatchFactory.optimize(trees);
		treeBatch.setName("trees");
		treeBatch.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
		root.attachChild(treeBatch);
	}
}
