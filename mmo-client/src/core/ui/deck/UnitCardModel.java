package core.ui.deck;

import com.jme3.animation.AnimControl;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import core.ui.PortraitData;
import core.ui.buffers.PortraitBuffer;
import core.ui.buffers.RenderingBuffer;
import program.main.Program;
import program.main.data.ClientDataLoader;
import shared.board.data.UnitData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class UnitCardModel extends CardModel {
	private static final int PORTRAIT_TEX_HEIGHT = 256;
	private static final float PORTRAIT_WIDTH_MUL = 0.67f;
	private static final Vector3f CAMERA_POSITION = new Vector3f(5, 5, 5);
	private static final Vector3f CAMERA_TARGET = new Vector3f(0, 1.5f, 0);

	private static final Map<String, PortraitBuffer> bridgeCache = new HashMap<String, PortraitBuffer>();

	private final UnitData unitData;

	public UnitCardModel(UnitData unitData, float size){
		super(Program.getInstance().getMainFrame().getAssetManager(), size);
		this.unitData = unitData;

		createContent();
	}

	@Override
	public void createContent() {
		Spatial model = ClientDataLoader.getUnitModel(unitData);
		RenderManager manager = Program.getInstance().getMainFrame().getRenderManager();
		PortraitBuffer buffer = bridgeCache.get(unitData.getName());

		if (buffer == null){
			buffer = new PortraitBuffer(manager, createPortraitData(model));
			bridgeCache.put(unitData.getName(), buffer);
		}

		material.setTexture("Portrait", buffer.getTexture());

		BitmapText bitmapText = new BitmapText(Program.getInstance().getMainFrame().getOutlinedFont(), false);
		bitmapText.setLocalRotation(new Quaternion().fromAngles(0, FastMath.PI, 0));
		bitmapText.setColor(new ColorRGBA(1f, 0.0f, 0.0f, 1f));
		bitmapText.setSize(0.05f);
		bitmapText.setQueueBucket(RenderQueue.Bucket.Transparent);
		bitmapText.setText(unitData.getName());
		bitmapText.setLocalTranslation(0, 0, -0.05f);

		//attachChild(bitmapText);
	}

	private PortraitData createPortraitData(Spatial model){
		Node node = createScene(model);

		PortraitData portraitData = new PortraitData();
		portraitData.setScene(node);
		portraitData.setWidth((int) (PORTRAIT_TEX_HEIGHT * PORTRAIT_WIDTH_MUL));
		portraitData.setHeight(PORTRAIT_TEX_HEIGHT);
		portraitData.setBackground(ColorRGBA.White);
		portraitData.setCameraLocation(CAMERA_POSITION);
		portraitData.setCameraTarget(CAMERA_TARGET);

		return portraitData;
	}

	private Node createScene(Spatial spatial){
		Node node = new Node();
		node.attachChild(spatial);

		spatial.getControl(AnimControl.class).createChannel().setAnim("stand");

		DirectionalLight light = new DirectionalLight();
		light.setColor(ColorRGBA.White);
		light.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
		node.addLight(light);

		AmbientLight ambientLight = new AmbientLight();
		ambientLight.setColor(ColorRGBA.White);
		node.addLight(ambientLight);

		return node;
	}

}
