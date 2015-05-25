package core.graphics;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * @author doc
 */
public class BoardCamera2 extends AbstractAppState {
	private Camera cam;
	private InputManager inputManager;

	private float tilt = FastMath.PI / 4f;
	private float rotation = FastMath.HALF_PI + FastMath.QUARTER_PI;
	private float distance = 55f;
	private Vector3f center = new Vector3f();

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		this.cam = app.getCamera();
		this.inputManager = app.getInputManager();
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);

		float x = distance * FastMath.cos(tilt) * FastMath.sin(rotation);
		float y = distance * FastMath.sin(tilt);
		float z = distance * FastMath.cos(tilt) * FastMath.cos(rotation);

		cam.setLocation(new Vector3f(x, y, z).addLocal(center));
		cam.lookAt(center, Vector3f.UNIT_Y);
	}

	public void setCenter(Vector3f center){
		this.center = center;
	}

	private void registerInput() {
		/*for (String mapping: mappings) {
			if (inputManager.hasMapping(mapping)) {
				inputManager.deleteMapping(mapping);
			}
		}

		inputManager.removeListener(listener);*/
	}

	@Override
	public void cleanup() {
		super.cleanup();
	}
}
