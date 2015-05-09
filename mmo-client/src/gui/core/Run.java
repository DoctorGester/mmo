package gui.core;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.system.AppSettings;
import gui.controls.Label;
import gui.controls.Panel;

import java.util.logging.*;

public class Run extends SimpleApplication {

	private Screen guiScreen;

	public Run() {
		Logger.getLogger("").setLevel(Level.ALL);
		AppSettings settings = new AppSettings(true);
		settings.setResolution(1093, 614);
		settings.setFrameRate(60);
		settings.setTitle("GUI Test App");
		settings.setSamples(4);
		settings.setVSync(true);
		setSettings(settings);
		setDisplayStatView(false);
		setShowSettings(false);

		start();
	}

	@Override
	public void simpleInitApp() {
		setPauseOnLostFocus(false);

		getAssetManager().registerLocator("", FileLocator.class);

		guiScreen = new Screen(this, "res/ui/atlas.png", "res/shaders/UIElement.j3md", "res/other/segoe.fnt");
		guiNode.addControl(guiScreen);

		flyCam.setEnabled(false);
		inputManager.setCursorVisible(true);

		cam.setFrustumFar(1000);

		createGUI();
	}

	private void createGUI() {
		ElementContainer container = guiScreen.getElementContainer();

		Panel panel = new Panel(V.f(40, 40), V.f(500, 300));
		//Button button = new Button(guiScreen, V.f(20, 20), V.f(128, 32));
		//button.setLayoutData(new CC().width("30%").maxWidth("100%").minWidth("0%").spanX());

		//panel.addElement(new Button(guiScreen, new CC().width("100%").spanX().height("32!")));
		/*panel.addElement(new Label(guiScreen, "test super", 20, V.f(0, 0), V.f(120, 20)));
		panel.addElement(new Button(guiScreen, V.f(20, 20), V.f(128, 32)));
		panel.addElement(new Button(guiScreen, V.f(20, 20), V.f(128, 32)));
		panel.addElement(new Label(guiScreen, "test malenki", 20, V.f(0, 0), V.f(120, 20)));
		panel.addElement(new Button(guiScreen, V.f(20, 20), V.f(128, 32)));
		panel.addElement(new Label(guiScreen, "test bolshoi", 20, V.f(0, 0), V.f(120, 20)));
		panel.addElement(new Label(guiScreen, "test malish", 20, V.f(0, 0), V.f(120, 20)));
		panel.addElement(new Button(guiScreen, V.f(20, 20), V.f(128, 32)));
		panel.addElement(new Label(guiScreen, "test text", 20, V.f(0, 0), V.f(120, 20)));*/

		//panel.addElement(new Label(guiScreen, "Menu", 20, new CC().alignX("center").growX().height("32!")));

		//Panel panel2 = new Panel(guiScreen, new CC().grow());

		/*panel2.addElement(new Button(guiScreen, new CC().grow()).setText("GOVNO", 20));
		panel2.addElement(new Button(guiScreen, new CC().grow()).setText("ATEZ", 20));
		panel2.addElement(new Button(guiScreen, new CC().grow()).setText("LOL", 20));
		panel2.addElement(new Button(guiScreen, new CC().grow()).setText("you", 20));*/

		/*panel2.addElement(new Panel(guiScreen, new CC().grow()));
		panel2.addElement(new Panel(guiScreen, new CC().grow()));
		panel2.addElement(new Panel(guiScreen, new CC().grow()));
		panel2.addElement(new Panel(guiScreen, new CC().grow()));

		panel.addElement(panel2);*/

		container.addElement(panel);

		/*Panel element = new Panel(V.f(20, 10), V.f(200, 100));
		panel.addElement(element);

		element.addElement(new Label("Meme\\#A00#zeeduh", 20, V.f(20, 20), V.f(120, 20)));*/

		panel.addElement(new Panel("id1"));
		panel.addElement(new Panel("id2"));
		panel.addElement(new Panel("id3"));
		panel.addElement(new Panel("id4"));
		panel.addElement(new Panel("id5"));
		panel.addElement(new Panel("id6"));
		panel.addElement(new Panel("id7"));
		/*panel.addElement(new Panel("id8"));
		panel.addElement(new Panel("id9"));
		panel.addElement(new Panel("idA"));
		panel.addElement(new Panel("idB"));
		panel.addElement(new Panel("idC"));
		panel.addElement(new Panel("idD"));
		panel.addElement(new Panel("idE"));*/

		panel.setLayout(
			new LayoutData("id1").w(50).h(40)
						.e("id2").w(70).h(40)
						.c("id3").fill().wrap()
						.e("id4").h(0.4f).fillX().span().skipBefore().wrap()
						.e("id5").w(50).h(40)
						.c("id6").wrap()
						.c("id7").fill().skipBefore().skipBefore().wrap(0)
		);

		/*panel.setLayout(
			new LayoutData("id1").spanX(2)
						.e("id2").spanY(6)
						.e("id3").spanY(3).skipBefore().wrap(3)
						.e("id4").spanY(4)
						.e("id5").spanX(4).wrap(3)
						.e("id6").skipBefore().spanX(3)
		);*/
	}

	public static void main(String ... args){
		new Run();
	}
}
