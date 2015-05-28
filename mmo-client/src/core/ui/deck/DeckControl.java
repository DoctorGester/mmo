package core.ui.deck;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.jme3.texture.Texture;
import core.graphics.CardMesh;

import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class DeckControl extends AbstractControl {
	private Node origin;

	private List<DeckElement> deckCards = new ArrayList<DeckElement>();
	private List<DeckElement> freeCards = new ArrayList<DeckElement>();

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
	private SimpleApplication application;

	public DeckControl(SimpleApplication application) {
		this.application = application;
		origin = new Node();

		CameraControl cameraControl = new CameraControl(application.getCamera());
		cameraControl.setControlDir(CameraControl.ControlDirection.CameraToSpatial);
		origin.addControl(cameraControl);

		application.getRootNode().attachChild(origin);
	}

	public void setDeck(int size){
		for (int i = 0; i < size; i++) {
			final float cardSize = 0.3f;
			Spatial model = new CardModel(application.getAssetManager(), cardSize);
			DeckElement element = new DeckElement(model);

			origin.attachChild(model);
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

		for (DeckElement card : freeCards)
			card.setProgressTarget(card.getProgressTarget() + value);
	}

	@Override
	protected void controlUpdate(float tpf) {
		int index = 0;

		for (DeckElement card : deckCards) {
			final float cardWidth = 0.005f;

			Vector3f deckBase = innerCurve[0].getPosition().clone();
			deckBase.z += index * cardWidth;

			card.getModel().setLocalTranslation(deckBase);
			card.getModel().setLocalRotation(new Quaternion().fromAngles(0, innerCurve[0].getRotation(), 0));

			index++;
		}

		List<DeckElement> addToEnd = new ArrayList<DeckElement>();
		List<DeckElement> addToStart = new ArrayList<DeckElement>();

		for (DeckElement card : freeCards) {
			final float speed = 0.25f * tpf;
			float sign = Math.signum(card.getProgressTarget() - card.getProgressCurrent());
			float progress = card.getProgressCurrent() + sign * speed;

			if (sign * (card.getProgressTarget() - card.getProgressCurrent()) < speed) {
				progress = card.getProgressTarget();
			}

			card.setProgressCurrent(progress);

			if (sign > 0 && progress >= 1.0f) {
				addToEnd.add(card);
			}

			if (sign < 0 && progress <= 0.0f) {
				addToStart.add(card);
			}

			ControlPoint point = getCardPosition(progress);
			card.getModel().setLocalTranslation(point.getPosition());
			card.getModel().setLocalRotation(new Quaternion().fromAngles(0, point.getRotation(), 0));
		}

		// Moving cards between view and deck

		final float space = 0.05f;

		if (freeCards.size() > 0 && deckCards.size() > 0) {
			while (true) {
				DeckElement lastFreeCard = freeCards.get(freeCards.size() - 1);

				if (lastFreeCard.getProgressCurrent() > space) {
					DeckElement addedCard = deckCards.remove(0);
					addedCard.setProgressTarget(lastFreeCard.getProgressTarget() - space);

					freeCards.add(addedCard);
				} else {
					break;
				}
			}
		}

		for (DeckElement card: addToEnd) {
			freeCards.remove(card);
			deckCards.add(card);

			card.setProgressCurrent(0.0f);
			card.setProgressTarget(0.0f);
		}

		for (DeckElement card: addToStart) {
			freeCards.remove(card);
			deckCards.add(0, card);

			card.setProgressCurrent(0.0f);
			card.setProgressTarget(0.0f);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
}
