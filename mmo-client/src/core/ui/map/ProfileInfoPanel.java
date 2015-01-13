package core.ui.map;

import com.jme3.font.BitmapFont;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import core.main.CardMaster;
import core.main.Faction;
import core.main.Stat;
import program.datastore.Data;
import program.datastore.DataStore;
import program.main.Program;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.utils.UIDUtil;

/**
 * @author doc
 */
public class ProfileInfoPanel extends Element implements Control {
	private static final float PANEL_WIDTH = 0.3f,
							   PANEL_HEIGHT = 0.68f,
							   HEADER_LABEL_HEIGHT = 0.05f,
							   ROW_HEIGHT = 0.04f,
							   PORTRAIT_HEIGHT = 0.3f,
							   STATS_HEIGHT = 0.20f,
							   FRACTION_HEIGHT = 0.3f,
							   X_OFFSET = 0.05f;

	private final Vector2f dimension;
	private final Vector2f panelSize;

	private long lastUpdateTime;

	private String searchKey;

	public ProfileInfoPanel(ElementManager screen) {
		super(screen, UIDUtil.getUID(), Vector2f.ZERO, Vector2f.ZERO, Vector4f.ZERO, null);

		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
		panelSize = new Vector2f(dimension.x * PANEL_WIDTH, dimension.y * PANEL_HEIGHT);

		setDimensions(screen.getWidth() * PANEL_WIDTH, screen.getHeight() * PANEL_HEIGHT);

		setIgnoreMouse(true);
		setInitialized();

		addControl(this);
	}

	public void setTarget(CardMaster target){
		searchKey = ProfileInfo.getKey(target.getId());
	}

	private void fromInfo(ProfileInfo info){
		Vector2f position, size;

		removeAllChildren();

		Vector2f nameLabelSize = new Vector2f(panelSize.x, panelSize.y * HEADER_LABEL_HEIGHT);

		Label nameLabel = new Label(screen, Vector2f.ZERO, nameLabelSize);
		nameLabel.setText(info.getName());
		nameLabel.setTextAlign(BitmapFont.Align.Center);
		nameLabel.setTextVAlign(BitmapFont.VAlign.Center);
		nameLabel.setFontSize(panelSize.y * HEADER_LABEL_HEIGHT);

		position = new Vector2f(0, panelSize.y * (HEADER_LABEL_HEIGHT + PORTRAIT_HEIGHT));
		size = new Vector2f(panelSize.x, panelSize.y * HEADER_LABEL_HEIGHT);

		Label stats = new Label(screen, position, size);
		stats.setText("Stats");
		stats.setTextAlign(BitmapFont.Align.Center);
		stats.setTextVAlign(BitmapFont.VAlign.Center);
		stats.setFontSize(panelSize.y * HEADER_LABEL_HEIGHT);

		int index = 1; // Skipping one row for the greater good

		for (Stat stat: Stat.values()){
			position = new Vector2f(panelSize.x * X_OFFSET,
									panelSize.y * (ROW_HEIGHT * index + HEADER_LABEL_HEIGHT * 2 + PORTRAIT_HEIGHT));
			size = new Vector2f(panelSize.x, panelSize.y * ROW_HEIGHT);

			Label label = new Label(screen, position, size);
			label.setText(stat.getName() + ": " + info.getStat(stat));
			label.setTextVAlign(BitmapFont.VAlign.Center);
			label.setFontSize(panelSize.y * ROW_HEIGHT);

			addChild(label);

			index++;
		}

		position = new Vector2f(0, panelSize.y * (HEADER_LABEL_HEIGHT * 2 + PORTRAIT_HEIGHT + STATS_HEIGHT));
		size = new Vector2f(panelSize.x, panelSize.y * HEADER_LABEL_HEIGHT);

		Label reputation = new Label(screen, position, size);
		reputation.setText("Reputation");
		reputation.setTextAlign(BitmapFont.Align.Center);
		reputation.setTextVAlign(BitmapFont.VAlign.Center);
		reputation.setFontSize(panelSize.y * HEADER_LABEL_HEIGHT);

		index = 1;

		for (Faction faction: Program.getInstance().getFactions().values()){
			position = new Vector2f(panelSize.x * X_OFFSET,
									panelSize.y * (ROW_HEIGHT * index + HEADER_LABEL_HEIGHT * 3 + PORTRAIT_HEIGHT + STATS_HEIGHT));
			size = new Vector2f(panelSize.x, panelSize.y * ROW_HEIGHT);

			Label label = new Label(screen, position, size);
			label.setText(faction.getName() + ": " + info.getReputation(faction));
			label.setTextVAlign(BitmapFont.VAlign.Center);
			label.setFontSize(panelSize.y * ROW_HEIGHT);

			addChild(label);

			index++;
		}

		addChild(nameLabel);
		addChild(stats);
		addChild(reputation);
	}

	@Override
	public void update(float tpf){
		Data data = DataStore.getInstance().getData(searchKey);

		if (data == null || data.getObject(ProfileInfo.class) == null)
			return;

		if (data.getDate() > lastUpdateTime) {
			lastUpdateTime = data.getDate();
			fromInfo(data.getObject(ProfileInfo.class));
		}
	}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		return this;
	}

	@Override
	public void setSpatial(Spatial spatial) {}

	@Override
	public void render(RenderManager rm, ViewPort vp) {}
}
