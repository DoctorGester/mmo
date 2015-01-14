package core.ui.battle;

import com.jme3.input.event.MouseButtonEvent;
import core.ui.UI;
import core.ui.UnitCardElement;
import program.main.Program;
import program.main.SceneUtil;
import tonegod.gui.core.Screen;
import tonegod.gui.listeners.MouseButtonListener;

public class UnitCardClickDispatcher implements MouseButtonListener{
	private Screen screen;
	private UnitCardElement cardElement;

	public UnitCardClickDispatcher(Screen screen, UnitCardElement cardElement) {
		this.screen = screen;
		this.cardElement = cardElement;
	}

	public void onMouseLeftPressed(MouseButtonEvent mouseButtonEvent) {
		SceneUtil.getUI(UI.STATE_BATTLE, BattleUIState.class).updateUnitUI(cardElement.getUnitData());
	}

	public void onMouseLeftReleased(MouseButtonEvent mouseButtonEvent) {
		if (screen.getDropElement() != null && cardElement.getCardId() != -1){
			Program.getInstance().getBattleController().battlePickCard(cardElement.getCard().getId());
		}
	}

	public void onMouseRightPressed(MouseButtonEvent mouseButtonEvent) {}
	public void onMouseRightReleased(MouseButtonEvent mouseButtonEvent) {}
}
