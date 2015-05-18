package core.graphics.scenes;

import com.jme3.animation.*;
import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import program.main.Program;
import program.main.data.ClientDataLoader;
import shared.other.DataLoaderKey;

/**
 * @author doc
 */
public class MenuScene extends AbstractScene {
	private DirectionalLight menuSceneSun;
	private boolean created = false;

	@Override
	public void setupCamera(Camera cam, InputManager inputManager) {}

	@Override
	public void setupLight(DirectionalLightShadowRenderer shadowRenderer) {
		if (menuSceneSun == null){
			menuSceneSun = new DirectionalLight();
			menuSceneSun.setColor(ColorRGBA.White);
			menuSceneSun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());

			root.addLight(menuSceneSun);
		}
		shadowRenderer.setLight(menuSceneSun);
	}

	@Override
	public void loadScene(SimpleApplication app) {
		if (!created){
			try {
				Node spatial = ClientDataLoader.loadAnimatedModelAlt("res/models/angel/angel_m00");
				AnimControl animControl = spatial.getControl(AnimControl.class);
				animControl.createChannel();

				AnimChannel channel = animControl.getChannel(0);
				channel.setAnim("run", 1f);
				channel.setLoopMode(LoopMode.Loop);

				root.attachChild(spatial);
			} catch (Exception e) {
				e.printStackTrace();
			}

			created = true;
		}

		/*Camera cam = app.getCamera();

		cam.setLocation(new Vector3f(5, 10, 5));
		cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
		cam.update();*/

		app.getRootNode().attachChild(root);
	}
}
