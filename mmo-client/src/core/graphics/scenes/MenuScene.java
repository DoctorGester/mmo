package core.graphics.scenes;

import com.jme3.animation.*;
import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import core.graphics.MaterialDebugAppState;
import core.graphics.PlaneShape;
import program.main.Program;
import program.main.data.DataLoader;

/**
 * @author doc
 */
public class MenuScene extends AbstractScene {
	private DirectionalLight menuSceneSun;
	private boolean created = false;
	private ChaseCamera camera;

	@Override
	public void setupCamera(ChaseCamera camera) {
		camera.setEnabled(false);
		this.camera = camera;
	}

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
			DataLoader dataLoader = Program.getInstance().getDataLoader();

			try {
				Node spatial = dataLoader.loadAnimatedModelAlt("res/models/angel/angel_m00");
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

		Camera cam = app.getCamera();

		cam.setLocation(new Vector3f(5, 10, 5));
		cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
		cam.update();

		app.getRootNode().attachChild(root);
	}

	@Override
	public void unloadScene(SimpleApplication app){
		camera.setEnabled(true);
		super.unloadScene(app);
	}
}
