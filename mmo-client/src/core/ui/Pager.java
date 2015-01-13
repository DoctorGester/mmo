package core.ui;

import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.core.Element;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.utils.UIDUtil;

/**
 * @author doc
 */
public abstract class Pager extends Element {
	private Label pageLabel;
	private Button forwardButton;
	private Button backwardButton;

	private int currentPage = 0;
	private int maxPages = 0;

	public Pager(ElementManager screen, Vector2f position, Vector2f dimensions) {
		super(screen, UIDUtil.getUID(), position, dimensions, Vector4f.ZERO, null);

		setIgnoreMouse(true);
		setInitialized();

		float padding = dimensions.x * 0.05f;

		Vector2f buttonSize = new Vector2f(dimensions.x * 0.35f - padding * 2, dimensions.y);
		Vector2f buttonPosition = new Vector2f(padding, 0);
		backwardButton = new ButtonAdapter(screen, buttonPosition, buttonSize){
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				super.onButtonMouseLeftUp(evt, toggled);
				setCurrentPage(currentPage - 1);
			}
		};
		backwardButton.setText("<");

		addChild(backwardButton);

		Vector2f labelSize = new Vector2f(dimensions.x * 0.3f - padding * 2, dimensions.y);
		Vector2f labelPosition = new Vector2f(buttonPosition.x + buttonSize.x + padding, 0);
		pageLabel = new Label(screen, labelPosition, labelSize);
		pageLabel.setTextAlign(BitmapFont.Align.Center);

		addChild(pageLabel);

		buttonSize = new Vector2f(dimensions.x * 0.35f - padding * 2, dimensions.y);
		buttonPosition = new Vector2f(labelPosition.x + labelSize.x + padding, 0);
		forwardButton = new ButtonAdapter(screen, buttonPosition, buttonSize){
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				super.onButtonMouseLeftUp(evt, toggled);

				setCurrentPage(currentPage + 1);
			}
		};
		forwardButton.setText(">");

		addChild(forwardButton);

		setMaxPages(5);
		setCurrentPage(1);
	}

	private void updatePageInterface(){
		pageLabel.setText(currentPage + "/" + maxPages);

		backwardButton.setIsEnabled(currentPage > 1);
		forwardButton.setIsEnabled(currentPage < maxPages);
	}

	public void setCurrentPage(int page){
		int old = currentPage;
		currentPage = Math.min(Math.max(1, page), maxPages);

		pageChanged(old, currentPage);

		updatePageInterface();
	}

	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;

		updatePageInterface();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public abstract void pageChanged(int from, int to);
}
