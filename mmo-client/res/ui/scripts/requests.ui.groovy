import com.jme3.input.event.MouseButtonEvent
import com.jme3.math.Vector4f
import core.ui.UI
import core.ui.map.MapUIState
import core.ui.map.RequestsUIState
import core.ui.map.requests.Request
import gui.core.V;
import program.main.SceneUtil
import tonegod.gui.controls.buttons.ButtonAdapter
import tonegod.gui.controls.scrolling.ScrollArea
import tonegod.gui.controls.scrolling.ScrollPanel
import tonegod.gui.controls.text.Label

def fillOutgoing(RequestsUIState state, double heightFrom) {
	final double BUTTON_SIZE = 0.09f;

	def panelWidth = state.panel.width
	def panelHeight = state.panel.height

	def index = 0

	synchronized (state.outgoingRequests) {
		for (final Request request : state.outgoingRequests) {
			def position = V.f(panelWidth * 0.03f, index * (state.ROW_HEIGHT * state.dimension.y) + panelHeight * heightFrom)
			def size = V.f(panelWidth * 0.75f, state.ROW_HEIGHT * state.dimension.y)

			def text = new Label(state.screen, position, size)
			text.text = request.descriptionOutgoing
			text.fontSize = state.screen.scaleFontSize(12f)

			size = V.f(panelHeight * BUTTON_SIZE, panelHeight * BUTTON_SIZE)
			position = V.f(panelWidth * 0.97f - (size.x + size.x * 0.03f), position.y + size.y / 4f)

			def cancel = new ButtonAdapter(state.screen, position, size) {
				void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
					request.cancel()
				}
			}
			cancel.setButtonIcon((size.x * 0.7f) as float, (size.y * 0.7f) as float, "res/textures/interface/reject.png")

			state.panel.addChild(text)
			state.panel.addChild(cancel)

			index++
		}
	}
}

def fillIncoming(RequestsUIState state, double startFrom, double height) {
	final double BUTTON_SIZE = 0.09f;
	final double ROW_HEIGHT = 0.1f;

	def index = 0
	def panelWidth = state.panel.width
	def panelHeight = state.panel.height

	synchronized (state.incomingRequests) {
		def scrollPanel = new ScrollPanel(
				state.screen,
				V.f(panelWidth * 0.03f, panelHeight * startFrom),
				V.f(panelWidth * 0.94f, panelHeight * height),
				Vector4f.ZERO,
				null
		)

		scrollPanel.scrollBounds

		def lst = new ArrayList<Request>(state.incomingRequests);
		lst.addAll(lst); lst.addAll(lst); lst.addAll(lst);
		for (final Request request : lst) {
			def position = V.f(panelWidth * 0.03f, index * (ROW_HEIGHT * panelHeight))
			def size = V.f(panelWidth * 0.5f, ROW_HEIGHT * panelHeight)

			def text = new Label(state.screen, position, size)

			text.text = request.descriptionIncoming
			text.fontSize = state.screen.scaleFontSize(12)

			size = V.f(panelHeight * BUTTON_SIZE, panelHeight * BUTTON_SIZE)
			position = V.f(scrollPanel.width * 0.97f - (size.x * 2 + size.x * 0.06f), position.y + size.y / 4f)

			def accept = new ButtonAdapter(state.screen, position, size) {
				public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
					request.accept()
				}
			}
			accept.setButtonIcon((size.x * 0.7f) as float, (size.y * 0.7f) as float, "res/textures/interface/accept.png")

			position = V.f(scrollPanel.width * 0.97f - (size.x + size.x * 0.03f), position.y)

			def reject = new ButtonAdapter(state.screen, position, size) {
				public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
					request.reject()
				}
			}
			reject.setButtonIcon((size.x * 0.7f) as float, (size.y * 0.7f) as float, "res/textures/interface/reject.png")

			scrollPanel.addScrollableContent(text)
			scrollPanel.addScrollableContent(accept)
			scrollPanel.addScrollableContent(reject)

			index++
		}

		state.panel.addChild(scrollPanel)
	}
}

def createHeader(RequestsUIState state, double height){
	def position = V.f(state.panel.width * 0.03f, state.panel.height * 0.03f)
	def size = V.f(state.panel.width, state.panel.height * height)

	def text = new Label(state.screen, position, size)
	text.text = "Requests and notifications"
	text.fontSize = state.screen.scaleFontSize(14)

	state.panel.addChild(text)
}

def create(RequestsUIState state) {
	state.panel.removeAllChildren()

	final float PANEL_WIDTH = 0.3;
	final float PANEL_HEIGHT = 0.4
	final float HEADER_HEIGHT = 0.08;
	final float OUTGOING_HEIGHT = 0.4;

	def panelSize = V.f(state.dimension.x * PANEL_WIDTH, state.dimension.y * PANEL_HEIGHT)
	def panelPosition = V.f(state.dimension.x * 0.03f, state.dimension.y * 0.5f)

	state.panel.position = panelPosition
	state.panel.dimensions = panelSize
	state.panel.setInitialized()

	createHeader(state, HEADER_HEIGHT);
	fillOutgoing(state, HEADER_HEIGHT)
	fillIncoming(state, HEADER_HEIGHT + 0.1, 0.5)

	// To hide child components
	state.panel.isVisible = state.panel.isVisible

	SceneUtil.getUI(UI.STATE_MAP_MAIN, MapUIState.class).updateLeftSide()
}


def clean(RequestsUIState state) {

}