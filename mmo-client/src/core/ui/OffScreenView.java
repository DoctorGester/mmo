/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.ui;

import com.jme3.input.ChaseCamera;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import program.main.Program;

/**
 * @author t0neg0d
 * @author doc
 */
public class OffScreenView extends AbstractControl {
	private RenderManager renderManager;
	private ChaseCamera chaseCamera;
	private Camera camera;
	private ViewPort viewPort;
	private Node root;
	private Texture2D texture;

	public OffScreenView(RenderManager renderManager, int width, int height, Node root) {
		this.renderManager = renderManager;
		this.root = root;

		camera = new Camera(width, height);

		viewPort = renderManager.createPreView("Offscreen View", camera);
		viewPort.setClearFlags(true, true, true);

		FrameBuffer offBuffer = new FrameBuffer(width, height, 1);

		texture = new Texture2D(width, height, Image.Format.RGBA8);
		texture.setMinFilter(Texture.MinFilter.Trilinear);
		texture.setMagFilter(Texture.MagFilter.Bilinear);

		offBuffer.setDepthBuffer(Image.Format.Depth);
		offBuffer.setColorTexture(texture);

		viewPort.setOutputFrameBuffer(offBuffer);

		setSpatial(root);
		viewPort.attachScene(root);

		chaseCamera = new ChaseCamera(camera, root) {
			@Override
			public void setDragToRotate(boolean dragToRotate) {
				this.dragToRotate = dragToRotate;
				this.canRotate = !dragToRotate;
			}
		};
		chaseCamera.setDefaultDistance(5f);
		chaseCamera.setMaxDistance(340f);
		chaseCamera.setDefaultHorizontalRotation(90 * FastMath.DEG_TO_RAD);
		chaseCamera.setDefaultVerticalRotation(0f);
		camera.setFrustumFar(36000f);
		float aspect = (float) camera.getWidth() / (float) camera.getHeight();
		camera.setFrustumPerspective(45f, aspect, 0.1f, camera.getFrustumFar());
		chaseCamera.setUpVector(Vector3f.UNIT_Y);

		Program.getInstance().getMainFrame().getGuiNode().addControl(this);
	}

	public Texture2D getTexture() {
		return this.texture;
	}

	public ViewPort getViewPort() {
		return this.viewPort;
	}

	public Camera getCamera() {
		return this.camera;
	}

	public ChaseCamera getChaseCamera() {
		return this.chaseCamera;
	}

	public RenderManager getRenderManager() {
		return this.renderManager;
	}

	public Node getRootNode() {
		return this.root;
	}

	public void setViewPortColor(ColorRGBA color) {
		viewPort.setBackgroundColor(color);
	}

	@Override
	public final void setSpatial(Spatial spatial) {
		this.spatial = spatial;
		if (spatial != null)
			this.setEnabled(true);
		else
			this.setEnabled(false);
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (enabled) {
			root.updateLogicalState(tpf);
			root.updateGeometricState();
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}

}
