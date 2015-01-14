package core.ui.battle;

import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import core.board.ClientBuff;
import shared.board.Buff;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import tonegod.gui.core.utils.UIDUtil;
import tonegod.gui.listeners.MouseFocusListener;

/**
 * @author doc
 */
public class BuffIcon extends Element implements MouseFocusListener{

	public BuffIcon(Screen screen, Vector2f position, Vector2f size, String image) {
		super(screen, UIDUtil.getUID(), position, size, new Vector4f(), image);
		setTextAlign(BitmapFont.Align.Center);
		setTextVAlign(BitmapFont.VAlign.Center);
		setFontSize(18);
		setFont("res/other/segoe_outlined.fnt");
	}

	public void setBuffData(ClientBuff buff){
		int ticks = buff.getTimeLeft();
		int repeats = buff.getTimesToRepeatLeft();

		String text = ticks + "/" + repeats;
		if (buff.isEndless())
			text = "";
		if (buff.isNotRepeated())
			text = String.valueOf(ticks);

		setText(text);
	}

	@Override
	public void onGetFocus(MouseMotionEvent evt) {
		setHasFocus(true);
	}

	@Override
	public void onLoseFocus(MouseMotionEvent evt) {
		setHasFocus(false);
	}
}
