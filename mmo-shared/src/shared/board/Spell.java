package shared.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import shared.board.data.SpellData;

public interface Spell {
	public static final int SCRIPT_EVENT_CAST_BEGIN = 0x00,
							SCRIPT_EVENT_CAST_END = 0x01;

	public Object callEvent(int event);
	public SpellData getSpellData();
	public Binding getScope();
	public Script getScript();
}
