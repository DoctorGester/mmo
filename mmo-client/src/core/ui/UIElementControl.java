package core.ui;

/**
 * @author doc
 */

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class UIElementControl extends AbstractControl {
	private Camera camera;
	private Vector3f offset;

	/**
	 * @param camera The Camera to be synced.
	 */
	public UIElementControl(Camera camera, Vector3f offset) {
		this.camera = camera;
		this.offset = offset;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (spatial != null && camera != null) {
			Vector3f transformedOffset = camera.getRotation().mult(offset);

			spatial.setLocalTranslation(camera.getLocation().add(transformedOffset));
			spatial.setLocalRotation(camera.getRotation());
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		// nothing to do
	}
}
