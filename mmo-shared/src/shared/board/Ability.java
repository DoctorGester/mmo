package shared.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import shared.board.data.AbilityData;

/**
 * @author doc
 */
public interface Ability {

	public static final int SCRIPT_EVENT_CHECK = 0x00,
							SCRIPT_EVENT_CAST = 0x01,
							SCRIPT_EVENT_CAST_BEGIN = 0x02,
							SCRIPT_EVENT_CAST_END = 0x03;

	public AbilityData getAbilityData();
	public Binding getScope();
	public Script getScript();
	public Unit getCaster();
	public int getCoolDownLeft();
	public boolean onCoolDown();
	public Object callEvent(int event, Cell target);
	public void updateCoolDown();
	public void putOnCoolDown();
}
