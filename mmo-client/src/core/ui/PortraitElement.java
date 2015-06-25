package core.ui;

import com.jme3.animation.AnimControl;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.utils.UIDUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class PortraitElement extends Element {
	private static final int PORTRAIT_TEX_HEIGHT = 256;
	private static final float PORTRAIT_WIDTH_MUL = 0.67f;

	private static final Map<String, RenderingBuffer> bridgeCache = new HashMap<String, RenderingBuffer>();

	private Node createNode(Spatial spatial){
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

	public PortraitElement(ElementManager screen, String key, float height, Spatial model, Vector3f cameraPosition, Vector3f cameraTarget) {
		super(screen, UIDUtil.getUID(), new Vector2f(), new Vector2f(height * PORTRAIT_WIDTH_MUL, height), new Vector4f(), null);

		RenderingBuffer buffer = bridgeCache.get(key);

		if (buffer == null){
			Node node = createNode(model);

			PortraitData portraitData = new PortraitData();
			portraitData.setScene(node);
			portraitData.setWidth((int) (PORTRAIT_TEX_HEIGHT * PORTRAIT_WIDTH_MUL));
			portraitData.setHeight(PORTRAIT_TEX_HEIGHT);
			portraitData.setBackground(ColorRGBA.White);
			portraitData.setCameraLocation(cameraPosition);
			portraitData.setCameraTarget(cameraTarget);

			buffer = new PortraitBuffer(screen.getApplication().getRenderManager(), portraitData);

			bridgeCache.put(key, buffer);
		}

		setOffScreenView(buffer);

		setIgnoreMouse(true);
	}

	public void setOffScreenView(RenderingBuffer buffer){
		getElementMaterial().setTexture("ColorMap", buffer.getTexture());
		getElementMaterial().setColor("Color", ColorRGBA.White);
	}
}
