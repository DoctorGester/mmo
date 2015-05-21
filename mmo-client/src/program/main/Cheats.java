package program.main;

import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import com.jme3.util.TempVars;
import core.board.ClientBoard;
import core.graphics.CardMesh;
import core.graphics.scenes.BattleScene;
import core.graphics.scenes.Scenes;
import core.ui.BattleState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class Cheats {
	private Program program;

	public Cheats(Program program){
		this.program = program;
	}

	public void init(){
		InputManager inputManager = program.getMainFrame().getInputManager();
		inputManager.addMapping("enter_battle", new KeyTrigger(KeyInput.KEY_B));
		inputManager.addListener(new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				ClientBoard board = new ClientBoard(8, 8);
				BattleState state = new BattleState();
				state.setBoard(board);

				SceneUtil.getScene(Scenes.BATTLE, BattleScene.class).setBattleState(state);

				program.getMainFrame().setScene(Scenes.BATTLE);
				program.getMainFrame().setUIState(null);
			}
		}, "enter_battle");

		inputManager.addMapping("card_control", new KeyTrigger(KeyInput.KEY_C));
		inputManager.addListener(new ActionListener() {
			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				Node root = program.getMainFrame().getRootNode();
				root.addControl(new CardControl(program.getMainFrame()));
			}
		}, "card_control");

	}

	private static class CardControl extends AbstractControl {
		private Node origin;

		private List<Geometry> cards = new ArrayList<Geometry>();

		public CardControl(SimpleApplication application) {
			origin = new Node();

			CameraControl cameraControl = new CameraControl(application.getCamera());
			cameraControl.setControlDir(CameraControl.ControlDirection.CameraToSpatial);
			origin.addControl(cameraControl);

			application.getRootNode().attachChild(origin);

			for (int i = 0; i < 50; i++){
				float size = 0.3f;
				Mesh mesh = new CardMesh(size * 0.67f, size);
				Geometry card = new Geometry("My Textured Box", mesh);
				card.setLocalTranslation(0, 0, 2f);
				Material cube1Mat = new Material(application.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				Texture cube1Tex = application.getAssetManager().loadTexture("res/textures/card.png");
				cube1Mat.setTexture("ColorMap", cube1Tex);
				card.setMaterial(cube1Mat);

				origin.attachChild(card);
				cards.add(card);
			}
		}

		@Override
		protected void controlUpdate(float tpf) {

			/*TempVars vars = TempVars.get();

			Vector3f vecDiff = vars.vect1.set(camera.getLocation()).subtractLocal(origin.getWorldTranslation());
			origin.setLocalTranslation(vecDiff.addLocal(origin.getLocalTranslation()));

			Quaternion worldDiff = vars.quat1.set(camera.getRotation()).subtractLocal(origin.getWorldRotation());
			worldDiff.addLocal(origin.getLocalRotation());
			origin.setLocalRotation(worldDiff);

			vars.release();*/
			for (Geometry card: cards)
				card.rotate(0.3f * tpf, 0, 0);
		}

		@Override
		protected void controlRender(RenderManager rm, ViewPort vp) {

		}
	}
}
