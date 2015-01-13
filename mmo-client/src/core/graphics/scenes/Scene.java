package core.graphics.scenes;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;

/**
 * @author doc
 */
public interface Scene {

	public void setupCamera(ChaseCamera camera);
	public void setupLight(DirectionalLightShadowRenderer shadowRenderer);
	public void setupInput(InputManager inputManager);
	public void loadScene(SimpleApplication app);
	public void unloadScene(SimpleApplication app);
	public void updateScene(SimpleApplication app, float tpf);
	public Node getRoot();

}
