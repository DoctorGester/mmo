package core.ui.battle;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import tonegod.gui.controls.scrolling.ScrollArea;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;

import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class BattleLog extends Panel {
	private static final Vector2f INSETS = new Vector2f(10f, 10f);
	private ScrollArea logArea;

	private static final int HISTORY_SIZE = 500;
	protected List<String> chatMessages = new LinkedList<String>();

	public BattleLog(Screen screen, Vector2f position, Vector2f dimensions) {
		super(screen, position, dimensions);

		setIsMovable(false);
		setIsResizable(false);

		logArea = new ScrollArea(screen, INSETS, getDimensions().subtract(INSETS.mult(2)), Vector4f.ZERO, null, true);

		logArea.setScaleEW(true);
		logArea.setScaleNS(true);
		logArea.setIsResizable(false);
		logArea.setText("");
		logArea.setFontSize(14);
		addChild(logArea);
	}

	public void addMsg(String msg) {
		chatMessages.add(msg);
		updateLogHistory();
	}

	private void updateLogHistory() {
		if (chatMessages.size() > HISTORY_SIZE)
			chatMessages.remove(0);
		rebuildLog();
	}

	private void rebuildLog() {
		String displayText = "";
		int index = 0;
		for (String s : chatMessages) {
			if (index > 0)
				displayText += "\n" + s;
			else
				displayText += s;
			index++;
		}
		logArea.setText(displayText);
		logArea.scrollToBottom();
	}
}