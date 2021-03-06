package core.ui.deck;

import com.jme3.animation.AnimControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Image;
import core.ui.PortraitData;
import core.ui.buffers.PortraitBuffer;
import core.ui.buffers.RenderingBuffer;
import core.ui.buffers.UIBuffer;
import program.main.Program;
import program.main.data.ClientDataLoader;
import shared.board.data.UnitData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class UnitCardModel extends CardModel {
	private static final int TEXTURE_UP_SCALE = 2;
	private static final int PORTRAIT_TEX_HEIGHT = 256;
	private static final float PORTRAIT_WIDTH_MUL = 0.67f;
	private static final int PORTRAIT_TEX_WIDTH = (int) (PORTRAIT_TEX_HEIGHT * PORTRAIT_WIDTH_MUL);
	private static final Vector3f CAMERA_POSITION = new Vector3f(5, 5, 5);
	private static final Vector3f CAMERA_TARGET = new Vector3f(0, 1.5f, 0);

	private static final Map<String, PortraitBuffer> bridgeCache = new HashMap<String, PortraitBuffer>();

	private final UnitData unitData;

	public UnitCardModel(UnitData unitData, float size){
		super(Program.getInstance().getMainFrame().getAssetManager(), size);
		this.unitData = unitData;
	}

	@Override
	public void createContent() {
		ClientDataLoader.getUnitModel(unitData, new ClientDataLoader.ModelLoadingTask() {
			@Override
			public void loaded(Node result) {
				RenderManager renderManager = Program.getInstance().getMainFrame().getRenderManager();
				PortraitBuffer buffer = bridgeCache.get(unitData.getName());
				UIBuffer uiBuffer = createUIBuffer(renderManager);

				if (buffer == null){
					buffer = new PortraitBuffer(renderManager, createPortraitData(result));
					bridgeCache.put(unitData.getName(), buffer);
				}

				material.setTexture("Portrait", buffer.getTexture());
				material.setTexture("Content", uiBuffer.getTexture());
			}
		});
	}

	private BitmapText createTextSimple(String text, float fontSize){
		BitmapText bitmapText = new BitmapText(Program.getInstance().getMainFrame().getOutlinedFont(), false);
		bitmapText.setColor(new ColorRGBA(1f, 0.0f, 0.0f, 1f));
		bitmapText.setSize(fontSize);
		bitmapText.setQueueBucket(RenderQueue.Bucket.Transparent);
		bitmapText.setText(text);
		bitmapText.setColor(ColorRGBA.White);
		bitmapText.setLocalTranslation(-bitmapText.getLineWidth() / 2f, 0, 0);

		return bitmapText;
	}

	private UIBuffer createUIBuffer(RenderManager manager){
		Node scene = new Node();

		String stats = String.format("%s/%s/%s", unitData.getDamage(), unitData.getActionPoints() - 1, unitData.getHealth());

		BitmapText nameText = createTextSimple(unitData.getName(), 0.7f);
		BitmapText statsText = createTextSimple(stats, 0.6f);
		statsText.setLocalTranslation(statsText.getLocalTranslation().setY(-1.2f));

		scene.attachChild(nameText);
		scene.attachChild(statsText);

		return new UIBuffer(manager, PORTRAIT_TEX_WIDTH * TEXTURE_UP_SCALE, PORTRAIT_TEX_HEIGHT * TEXTURE_UP_SCALE, scene);
	}

	private PortraitData createPortraitData(Spatial model){
		Node node = createScene(model);

		PortraitData portraitData = new PortraitData();
		portraitData.setScene(node);
		portraitData.setWidth(PORTRAIT_TEX_WIDTH * TEXTURE_UP_SCALE);
		portraitData.setHeight(PORTRAIT_TEX_HEIGHT * TEXTURE_UP_SCALE);
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
