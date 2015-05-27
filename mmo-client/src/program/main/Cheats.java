package program.main;

import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.jme3.texture.Texture;
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
	private CardControl cardControl;

	public Cheats(Program program) {
		this.program = program;
	}

	public void init() {
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
				if (!isPressed)
					return;

				Node root = program.getMainFrame().getRootNode();
				cardControl = new CardControl(program.getMainFrame());
				root.addControl(cardControl);
			}
		}, "card_control");

		inputManager.addMapping("+wheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addMapping("-wheel", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
		inputManager.addListener(new AnalogListener() {
			@Override
			public void onAnalog(String name, float value, float tpf) {
				if (name.startsWith("+"))
					cardControl.spin(value * tpf);
				else
					cardControl.spin(-value * tpf);
			}
		}, "+wheel", "-wheel");
	}

	private static class CardControl extends AbstractControl {

		private class CardElement {
			private Geometry geometry;
			private float progressCurrent;
			private float progressTarget;
			private int order;
		}

		private class ControlPoint {
			private Vector3f position;
			private float rotation;

			public ControlPoint(Vector3f position, float rotation) {
				this.position = position;
				this.rotation = rotation;
			}

			public void setPosition(Vector3f position) {
				this.position = position;
			}

			public void setRotation(float rotation) {
				this.rotation = rotation;
			}

			public Vector3f getPosition() {
				return position;
			}

			public float getRotation() {
				return rotation;
			}
		}

		private Node origin;

		private List<CardElement> deckCards = new ArrayList<CardElement>();
		private List<CardElement> freeCards = new ArrayList<CardElement>();

		private ControlPoint[] innerCurve = new ControlPoint[]{
				new ControlPoint(new Vector3f(1.2f, 0, 2f), FastMath.PI),
				new ControlPoint(new Vector3f(0.75f, 0, 1.5f), 0f),
				new ControlPoint(new Vector3f(-0.4f, 0, 1.5f), 0f),
				new ControlPoint(new Vector3f(-1.2f, 0, 2.2f), 0f)
		};

		private ControlPoint[] outerCurve = new ControlPoint[]{
				new ControlPoint(new Vector3f(1.2f, 0, 2.1f), FastMath.PI),
				new ControlPoint(new Vector3f(0.75f, 0f, 2.5f), FastMath.PI),
				new ControlPoint(new Vector3f(-0.4f, 0f, 2.5f), FastMath.PI),
				new ControlPoint(new Vector3f(-1.2f, 0, 2.2f), 0f),
		};

		public CardControl(SimpleApplication application) {
			origin = new Node();

			CameraControl cameraControl = new CameraControl(application.getCamera());
			cameraControl.setControlDir(CameraControl.ControlDirection.CameraToSpatial);
			origin.addControl(cameraControl);

			application.getRootNode().attachChild(origin);

			for (int i = 0; i < 20; i++) {
				final float size = 0.3f;
				Mesh mesh = new CardMesh(size * 0.67f, size);
				Geometry card = new Geometry("My Textured Box", mesh);
				Material cube1Mat = new Material(application.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				Texture cube1Tex = application.getAssetManager().loadTexture("res/textures/card.png");
				cube1Mat.setTexture("ColorMap", cube1Tex);
				card.setMaterial(cube1Mat);

				origin.attachChild(card);

				CardElement element = new CardElement();
				element.geometry = card;
				element.order = i;

				deckCards.add(element);
			}
		}

		private ControlPoint getCardPosition(float at) {
			at = FastMath.clamp(at, 0, 1);

			float curveProgress = at / 0.5f;
			ControlPoint[] curve = innerCurve;

			if (at >= 0.5f) {
				curve = outerCurve;
				curveProgress = 1.0f - (at - 0.5f) / 0.5f;
			}

			Vector3f position = FastMath.interpolateBezier(
					curveProgress,
					curve[0].getPosition(),
					curve[1].getPosition(),
					curve[2].getPosition(),
					curve[3].getPosition()
			);

			float rotation = FastMath.interpolateBezier(
					curveProgress,
					curve[0].getRotation(),
					curve[1].getRotation(),
					curve[2].getRotation(),
					curve[3].getRotation()
			);

			return new ControlPoint(position, rotation);
		}

		public void spin(float value) {
			if (freeCards.size() == 0 && value > 0)
				freeCards.add(deckCards.remove(0));

			for (CardElement card : freeCards)
				card.progressTarget += value;
		}

		@Override
		protected void controlUpdate(float tpf) {
			int index = 0;

			for (CardElement card : deckCards) {
				final float cardWidth = 0.005f;

				Vector3f deckBase = innerCurve[0].getPosition().clone();
				deckBase.z += index * cardWidth;

				card.geometry.setLocalTranslation(deckBase);
				card.geometry.setLocalRotation(new Quaternion().fromAngles(0, innerCurve[0].getRotation(), 0));

				index++;
			}

			List<CardElement> addToEnd = new ArrayList<CardElement>();
			List<CardElement> addToStart = new ArrayList<CardElement>();

			for (CardElement card : freeCards) {
				final float speed = 0.25f * tpf;
				float sign = Math.signum(card.progressTarget - card.progressCurrent);
				float progress = card.progressCurrent + sign * speed;

				if (sign * (card.progressTarget - card.progressCurrent) < speed) {
					progress = card.progressTarget;
				}

				card.progressCurrent = progress;

				if (sign > 0 && progress >= 1.0f) {
					addToEnd.add(card);
				}

				if (sign < 0 && progress <= 0.0f) {
					addToStart.add(card);
				}

				ControlPoint point = getCardPosition(progress);
				card.geometry.setLocalTranslation(point.getPosition());
				card.geometry.setLocalRotation(new Quaternion().fromAngles(0, point.getRotation(), 0));
			}

			// Moving cards between view and deck

			final float space = 0.05f;

			if (freeCards.size() > 0 && deckCards.size() > 0) {
				CardElement lastFreeCard = freeCards.get(freeCards.size() - 1);

				if (lastFreeCard.progressCurrent > space) {
					CardElement addedCard = deckCards.remove(0);
					addedCard.progressTarget = lastFreeCard.progressTarget - space;

					freeCards.add(addedCard);
				}
			}

			for (CardElement card: addToEnd) {
				freeCards.remove(card);
				deckCards.add(card);

				card.progressCurrent = 0.0f;
				card.progressTarget = 0.0f;
			}

			for (CardElement card: addToStart) {
				freeCards.remove(card);
				deckCards.add(0, card);

				card.progressCurrent = 0.0f;
				card.progressTarget = 0.0f;
			}
		}

		@Override
		protected void controlRender(RenderManager rm, ViewPort vp) {

		}
	}
}
