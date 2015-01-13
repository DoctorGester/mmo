package core.board.turns;

/**
 * @author doc
 */
public interface Turn {
	public static final int MODE_FIRST_STEP = 0x01,
							MODE_LAST_STEP = 0x02;

	public void execute(int mode);
	public void update(float tpf);
	public boolean hasLastStep();
	public boolean firstStepFinished();
	public String toStringRepresentation();
}
