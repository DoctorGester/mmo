package core.ui.battle;

import com.jme3.input.event.MouseButtonEvent;
import core.ui.SpellCardElement;
import core.ui.UI;
import program.main.Program;
import program.main.Util;
import tonegod.gui.core.Screen;
import tonegod.gui.listeners.MouseButtonListener;

public class SpellCardClickDispatcher implements MouseButtonListener{
	private Screen screen;
	private SpellCardElement cardElement;

	public SpellCardClickDispatcher(Screen screen, SpellCardElement cardElement) {
		this.screen = screen;
		this.cardElement = cardElement;
	}

	public void onMouseLeftReleased(MouseButtonEvent mouseButtonEvent) {
		if (screen.getDragElement() == cardElement
				&& screen.getDropElement() == Util.getUI(UI.STATE_SPELL_SELECTOR, SpellSelectorUIState.class).getDropPanel()
				&& cardElement.getCard() != null){
			Program.getInstance().getBattleController().battleCastCard(cardElement.getCard());
		}
	}

	public void onMouseLeftPressed(MouseButtonEvent mouseButtonEvent) {}
	public void onMouseRightPressed(MouseButtonEvent mouseButtonEvent) {}
	public void onMouseRightReleased(MouseButtonEvent mouseButtonEvent) {}
}
