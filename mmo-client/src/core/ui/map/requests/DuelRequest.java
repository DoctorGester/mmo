package core.ui.map.requests;

import program.main.Program;
import shared.map.CardMaster;

public class DuelRequest extends Request {
	public DuelRequest(CardMaster from, CardMaster to) {
		super(from, to);
	}

	@Override
	public String getDescriptionOutgoing() {
		return "Duel to " + getTo().getName();
	}

	@Override
	public String getDescriptionIncoming() {
		return "Duel from " + getFrom().getName();
	}

	@Override
	public void accept() {
		Program.getInstance().getMapController().acceptDuel(getFrom());
	}

	@Override
	public void reject() {
		Program.getInstance().getMapController().rejectDuel(getFrom());
	}

	@Override
	public void cancel() {
		Program.getInstance().getMapController().cancelDuel();
	}
}
