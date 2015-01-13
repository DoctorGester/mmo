package core.board.interfaces;

import core.board.SpellData;
import groovy.lang.Binding;
import groovy.lang.Script;

/**
 * @author doc
 */
public interface Spell {

	public static final int SCRIPT_EVENT_CHECK = 0x00,
							SCRIPT_EVENT_CAST = 0x01;

	public SpellData getSpellData();
	public Binding getScope();
	public Script getScript();
	public Unit getCaster();
	public int getCoolDownLeft();
	public boolean onCoolDown();
	public Object callEvent(int event, Cell target);
	public void updateCoolDown();
	public void putOnCoolDown();
}
