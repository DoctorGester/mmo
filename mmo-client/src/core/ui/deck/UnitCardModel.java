package core.ui.deck;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.queue.RenderQueue;
import program.main.Program;
import shared.board.data.UnitData;

/**
 * @author doc
 */
public class UnitCardModel extends CardModel {
	private final UnitData unitData;

	public UnitCardModel(UnitData unitData, float size){
		super(Program.getInstance().getMainFrame().getAssetManager(), size);
		this.unitData = unitData;

		createContent();
	}

	@Override
	public void createContent() {
		BitmapText bitmapText = new BitmapText(Program.getInstance().getMainFrame().getOutlinedFont(), false);
		bitmapText.setLocalRotation(new Quaternion().fromAngles(0, FastMath.PI, 0));
		bitmapText.setColor(new ColorRGBA(1f, 0.0f, 0.0f, 1f));
		bitmapText.setSize(0.05f);
		bitmapText.setQueueBucket(RenderQueue.Bucket.Transparent);
		bitmapText.setText(unitData.getName());
		bitmapText.setLocalTranslation(0, 0, -0.05f);

		//attachChild(bitmapText);
	}
}
