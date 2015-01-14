package shared.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import shared.board.data.CardSpellData;

public interface CardSpell {
	public static final int SCRIPT_EVENT_CAST_BEGIN = 0x00,
							SCRIPT_EVENT_CAST_END = 0x01;

	public Object callEvent(int event);
	public CardSpellData getSpellData();
	public Binding getScope();
	public Script getScript();
}
