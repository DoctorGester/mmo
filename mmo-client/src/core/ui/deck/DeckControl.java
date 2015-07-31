package core.ui.deck;

import com.jme3.app.SimpleApplication;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.simsilica.lemur.event.CursorEventControl;
import shared.items.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class DeckControl extends AbstractControl {
	private Node origin;

	private List<DeckElement> deckCards = new ArrayList<DeckElement>();
	private List<DeckElement> freeCards = new ArrayList<DeckElement>();

	private boolean hoverEnabled = false;

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

	public void setDeck(List<Item> deck){
		for (Item item: deck) {
			final float cardSize = 0.3f;

			Spatial model = CardModelFactory.createModel(item, cardSize);
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

	private DeckElement extractTopCard(){
		DeckElement card = deckCards.remove(0);
		freeCards.add(card);

		if (hoverEnabled)
			card.registerHoverListener();

		return card;
	}

	public void spin(float value) {
		if (freeCards.size() == 0 && value > 0)
			extractTopCard();

		for (DeckElement card : freeCards)
			card.setProgressTarget(card.getProgressTarget() + value);
	}

	public void enableElementHover(){
		hoverEnabled = true;

		for (DeckElement element: freeCards) {
			element.registerHoverListener();
		}
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
			float sign = card.updateMovement(speed, 2);
			float progress = card.getProgressCurrent();

			if (sign > 0 && progress >= 1.0f) {
				addToEnd.add(card);
			}

			if (sign < 0 && progress <= 0.0f) {
				addToStart.add(card);
			}

			ControlPoint point = getCardPosition(progress);

			if (point.getRotation() < FastMath.HALF_PI + FastMath.INV_TWO_PI) {
				card.updateHover(speed * 18);
			}

			Vector3f resultPosition = point.getPosition();

			// Extrapolate hover step to tangent function graph
			// Move card closer to the camera
			// Move card closer to the center
			// Scale it

			float diff = -resultPosition.x;
			float hoverStep = FastMath.tan(card.getHoverStep()) / FastMath.HALF_PI;
			float floatingStep = card.getFloatingStep();

			resultPosition.addLocal(0, 0, floatingStep * 0.02f);
			resultPosition.addLocal(0, 0, -hoverStep * 0.5f);
			resultPosition.addLocal(diff * hoverStep * 0.2f, 0, 0);

			Quaternion rotationTarget = new Quaternion().fromAngles(0, point.getRotation(), 0);
			Quaternion rotationHover = new Quaternion().fromAngles(0, 0, floatingStep * 0.035f - 0.017f);
			Quaternion rotationResult = new Quaternion().slerp(rotationTarget, rotationHover, hoverStep);

			card.getModel().setLocalTranslation(resultPosition);
			card.getModel().setLocalRotation(rotationResult);
			card.getModel().setLocalScale(1 + hoverStep * 0.65f);
		}

		// Moving cards between view and deck

		final float space = 0.05f;

		if (freeCards.size() > 0 && deckCards.size() > 0) {
			while (true) {
				DeckElement lastFreeCard = freeCards.get(freeCards.size() - 1);

				if (lastFreeCard.getProgressCurrent() > space) {
					DeckElement addedCard = extractTopCard();
					addedCard.setProgressTarget(lastFreeCard.getProgressTarget() - space);
				} else {
					break;
				}
			}
		}

		for (DeckElement card: addToEnd) {
			freeCards.remove(card);
			deckCards.add(card);

			if (hoverEnabled)
				card.removeHoverListener();

			card.setProgressCurrent(0.0f);
			card.setProgressTarget(0.0f);
		}

		for (DeckElement card: addToStart) {
			freeCards.remove(card);
			deckCards.add(0, card);

			if (hoverEnabled)
				card.removeHoverListener();

			card.setProgressCurrent(0.0f);
			card.setProgressTarget(0.0f);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {

	}
}
