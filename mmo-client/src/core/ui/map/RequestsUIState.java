package core.ui.map;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import core.main.CardMaster;
import core.ui.ScriptableUIState;
import core.ui.map.requests.Request;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author doc
 */
public class RequestsUIState extends ScriptableUIState {
	public static final float ROW_HEIGHT = 0.05f;
	public static final float PANEL_WIDTH = 0.30f;
	private static final float BUTTON_SIZE = 0.06f;

	private Panel mainPanel;

	private final List<Request> outgoingRequests = new LinkedList<Request>();
	private final List<Request> incomingRequests = new LinkedList<Request>();

	public RequestsUIState() {
		super("requests.ui.groovy");
	}

	public void initialize(AppStateManager stateManager, Application app) {
		mainPanel = new Panel(screen, Vector2f.ZERO);

		mainPanel.setIgnoreMouse(true);
		mainPanel.setIsVisible(false);

		update();

		screen.addElement(mainPanel, true);

		super.initialize(stateManager, app);
	}

	public void update(){
		callScript("create");
	}

	public List<Request> getOutgoingRequestsBySender(CardMaster sender){
		List<Request> result = new LinkedList<Request>();

		synchronized (outgoingRequests){
			for (Request request: outgoingRequests)
				if (request.getFrom() == sender)
					result.add(request);
		}

		return result;
	}

	public Request getSingleOutgoingRequest(CardMaster sender){
		List<Request> result = getOutgoingRequestsBySender(sender);

		if (!result.isEmpty())
			return result.get(0);

		return null;
	}

	public void addOutgoingRequest(Request request, boolean replace){
		synchronized (outgoingRequests){
			if (replace){
				Iterator<Request> iterator = outgoingRequests.iterator();

				while(iterator.hasNext()){
					Request found = iterator.next();

					if (found.getClass().equals(request.getClass()))
						iterator.remove();
				}
			}

			outgoingRequests.add(request);
		}

		update();
	}

	public void removeOutgoingRequest(Request request){
		synchronized (outgoingRequests){
			outgoingRequests.remove(request);
		}

		update();
	}

	public void addIncomingRequest(Request request){
		synchronized (incomingRequests){
			incomingRequests.add(request);
		}

		update();
	}

	public void removeIncomingRequest(Request request){
		synchronized (incomingRequests){
			incomingRequests.remove(request);
		}

		update();
	}

	public List<Request> getOutgoingRequests() {
		return outgoingRequests;
	}

	public List<Request> getIncomingRequests() {
		return incomingRequests;
	}

	public void showPanel(boolean show){
		mainPanel.setIsVisible(show);
	}

	public boolean isPanelVisible(){
		return mainPanel.getIsVisible();
	}

	public Panel getPanel(){
		return mainPanel;
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(mainPanel);
	}
}