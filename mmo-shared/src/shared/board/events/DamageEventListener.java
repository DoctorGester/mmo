package shared.board.events;

public interface DamageEventListener {
	public void onDamageTaken(DamageEventContext context);
}
