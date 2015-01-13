package core.graphics.scenes;

import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.scene.Node;

/**
 * @author doc
 */
public abstract class AbstractScene implements Scene{
	protected Node root = new Node();

	private ActionListener actionListener;
	private String mapping[];

	public void setActionListener(ActionListener actionListener, String ... mapping){
		this.actionListener = actionListener;
		this.mapping = mapping;
	}

	@Override
	public void setupInput(InputManager inputManager) {
		if (actionListener != null)
			inputManager.addListener(actionListener, mapping);
	}

	@Override
	public void unloadScene(SimpleApplication app) {
		root.removeFromParent();
		if (actionListener != null)
			app.getInputManager().removeListener(actionListener);
	}

	@Override
	public Node getRoot(){
		return root;
	}

	@Override
	public void updateScene(SimpleApplication app, float tpf) {}
}
