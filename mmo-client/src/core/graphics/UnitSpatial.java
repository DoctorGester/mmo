package core.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import core.board.ClientBoard;
import core.board.ClientUnit;
import program.main.Program;
import shared.board.Cell;
import shared.board.Unit;

public class UnitSpatial {
	private static final float BAR_SIZE = 2.8f;

	private ClientUnit unit;
	private Node node;
	private Spatial spatial;
	private Geometry geometry;

	// For units bars
	private Node bars;

	private Spatial healthBar;
	private Material healthBarMaterial;
	private float health;

	private Spatial actionBar;
	private Material actionBarMaterial;
	private float actions;

	private float crossProgress = 0.0f;

	private AnimControl animControl;
	private AnimChannel animChannel;
	private Material selectionQuadMaterial;

	private boolean wasSelected = false;
	private Geometry selectionQuad;

	public UnitSpatial(Spatial spatial, ClientUnit unit){
		this.node = new Node("unit");
		this.spatial = spatial;
		this.unit = unit;

		this.node.attachChild(spatial);

		animControl = spatial.getControl(AnimControl.class);
		if (animControl != null)
			animChannel = animControl.createChannel();

		// Selecting main geometry from our model
		Node node = (Node) spatial;
		for(Spatial child : node.getChildren())
			if (child instanceof Geometry){
				geometry = (Geometry) child;
				break;
			}

		animChannel.setAnim("stand", 0.f);
		geometry.updateModelBound();
		/*spatial.updateModelBound();

		WireBox b = new WireBox();
		b.fromBoundingBox((BoundingBox) spatial.getWorldBound());

		Box mesh = new Box(new Vector3f(-1, 0, -1), new Vector3f(1, 3, 1));
		mesh.setMode(Mesh.Mode.Lines);

		Geometry bx = new Geometry("TheMesh", mesh);
		Material mat_box = new Material(Program.getInstance().getMainFrame().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat_box.setColor("Color", ColorRGBA.Red);

		bx.setMaterial(mat_box);
		bx.setLocalScale(Cell.CELL_WIDTH * 1.5f);

		node.attachChild(bx);

		float mul = Cell.CELL_WIDTH * 0.1f;
		BoundingBox box = new BoundingBox(new Vector3f(-1, 0, -1).mult(mul), new Vector3f(1, 3, 1).mult(mul));
		spatial.setModelBound(box);*/

		createSelectionQuad();
	}

	private void createSelectionQuad(){
		int playerId = unit.getOwner().getBattleId();
		AssetManager assetManager = Program.getInstance().getMainFrame().getAssetManager();

		selectionQuadMaterial = new Material(assetManager, "res/shaders/ColoredCell.j3md");
		selectionQuadMaterial.setColor("Specular", ColorRGBA.White);
		selectionQuadMaterial.setColor("Front", ClientBoard.PLAYER_COLORS[playerId]);
		selectionQuadMaterial.setColor("Back", ColorRGBA.LightGray);
		selectionQuadMaterial.setTexture("DiffuseMap", assetManager.loadTexture("res/textures/selection.png"));
		selectionQuadMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		//selectionQuadMaterial.setTexture("AlphaMap", assetManager.loadTexture("res/textures/selection.png"));
		//selectionQuadMaterial.setFloat("AlphaDiscardThreshold", 0.5f);

		float quadSize = Cell.CELL_WIDTH * 0.1f;

		Quad mesh = new Quad(quadSize - 0.1f, quadSize - 0.1f);
		selectionQuad = new Geometry("Quad", mesh);
		selectionQuad.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, 0, 0));
		selectionQuad.setLocalTranslation(-quadSize / 2f, 0.05f, quadSize / 2f);
		selectionQuad.setShadowMode(RenderQueue.ShadowMode.Receive);
		selectionQuad.setMaterial(selectionQuadMaterial);
		selectionQuad.setQueueBucket(RenderQueue.Bucket.Transparent);

		node.attachChild(selectionQuad);
	}

	public void setSelectionVisible(boolean visible){
		if (!visible && selectionQuad.getParent() != null)
			selectionQuad.removeFromParent();
		else if (visible && selectionQuad.getParent() == null)
			node.attachChild(selectionQuad);
	}

	public void createBars(AssetManager assetManager){
		boolean ally = unit.getBoard().areAllies(Program.getInstance().getMainPlayer(), unit.getOwner());

		bars = new Node();

		PlaneShape shape = new PlaneShape(BAR_SIZE / 2f, 0.2f);//new Box(BAR_SIZE / 2f, 0.15f, 0.05f);

		healthBarMaterial = new Material(assetManager, "res/shaders/Bar.j3md");
		healthBarMaterial.setColor("Color", new ColorRGBA(ally ? 0 : 0.65f, ally ? 0.65f : 0, 0, 0.7f));
		healthBarMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

		healthBar = new Geometry("hpBar", shape);
		healthBar.setLocalTranslation(0, 0.25f, 0);
		healthBar.setMaterial(healthBarMaterial);

		shape = new PlaneShape(BAR_SIZE / 2f, 0.12f);

		actionBarMaterial = new Material(assetManager, "res/shaders/Bar.j3md");
		actionBarMaterial.setColor("Color", new ColorRGBA(1, 0.65f, 0, 0.7f));
		actionBarMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

		actionBar = new Geometry("acBar", shape);
		actionBar.setLocalTranslation(0, -0.25f, 0);
		actionBar.setMaterial(actionBarMaterial);

		bars.attachChild(healthBar);
		bars.attachChild(actionBar);

		updateMaterials();

		bars.setLocalTranslation(0, 6.2f, 0);

		node.attachChild(bars);

		setBarsStyle(0);
	}

	public void setBarsStyle(int style){
		bars.removeControl(BillboardControl.class);
		switch (style){
			case 1:
				BillboardControl control = new BillboardControl();
				control.setAlignment(BillboardControl.Alignment.AxialY);
				bars.addControl(control);
				break;
			case 0:
				bars.addControl(new BillboardControl());
		}
	}

	public void setSelected(boolean selected){
		if (wasSelected != selected){
			AssetManager assetManager = Program.getInstance().getMainFrame().getAssetManager();
			selectionQuadMaterial.setTexture("GlowMap", selected ? assetManager.loadTexture("res/textures/selection.png") :  null);
			wasSelected = selected;
		}
	}

	public void updateBars(){
		float healthReal = (float) unit.getCurrentHealth() / unit.getMaxHealth();

		// Updating bars values
		if (health != healthReal){
			if (healthReal > health)
				health = Math.min(health + 0.015f, healthReal);
			else
				health = Math.max(health - 0.015f, healthReal);

			updateMaterials();
		}

		float actionsReal;
		if (unit.getState() == Unit.STATE_REST)
			actionsReal = 1 - (float) unit.getRestLeft() / unit.getRestTime();
		else
			actionsReal = (float) unit.getCurrentActionPoints() / unit.getMaxActionPoints();

		if (actions != actionsReal){
			if (actionsReal > actions)
				actions = Math.min(actions + 0.025f, actionsReal);
			else
				actions = Math.max(actions - 0.025f, actionsReal);

			updateMaterials();
		}

		if (unit.getState() == Unit.STATE_DEAD && health == 0 && actions == 0 && crossProgress < 1){
			crossProgress = Math.min(crossProgress + 0.007f, 1);

			// 0.01f because of flickering at intersection point
			actionBar.setLocalTranslation(0, 0.25f * crossProgress, 0.01f);
			actionBar.setLocalRotation(new Quaternion().fromAngles(0, 0, crossProgress * FastMath.QUARTER_PI));

			healthBar.setLocalRotation(new Quaternion().fromAngles(0, 0, crossProgress * -FastMath.QUARTER_PI));
		}
	}

	private void updateMaterials(){
		healthBarMaterial.setFloat("Value", health * unit.getMaxHealth());
		healthBarMaterial.setFloat("MaxValue", unit.getMaxHealth());
		healthBarMaterial.setFloat("ValuePerBar", 4);

		actionBarMaterial.setFloat("Value", actions * unit.getMaxActionPoints());
		actionBarMaterial.setFloat("MaxValue", unit.getMaxActionPoints());
		actionBarMaterial.setFloat("ValuePerBar", 1);
	}

	public ClientUnit getUnit() {
		return unit;
	}

	public Node getNode() {
		return node;
	}

	public Spatial getSpatial() {
		return spatial;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public AnimChannel getAnimChannel(){
		return animChannel;
	}

	public void setScale(double scale) {
		node.setLocalScale((float) scale);
	}

	public void debug(MaterialDebugAppState state){
		state.registerBinding("res/shaders/Bar.frag", healthBar);
		state.registerBinding("res/shaders/Bar.frag", actionBar);
	}
}
