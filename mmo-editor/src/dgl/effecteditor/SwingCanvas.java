package dgl.effecteditor;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.ChaseCamera;
import com.jme3.math.FastMath;
import com.jme3.system.AppSettings;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;
import com.jme3.system.awt.PaintMode;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class SwingCanvas extends SimpleApplication {
	private AwtPanel panel;
	private GroovyObject script;

	public SwingCanvas(){
		super();
		setShowSettings(false);

		AppSettings settings = new AppSettings(true);
		settings.setCustomRenderer(AwtPanelsContext.class);
		settings.setFrameRate(60);
		settings.setSamples(4);
		settings.setVSync(true);
		setSettings(settings);

		createCanvas();
		startCanvas();

		AwtPanelsContext ctx = (AwtPanelsContext) getContext();
		panel = ctx.createPanel(PaintMode.Accelerated);
		ctx.setInputSource(panel);
		ctx.setSystemListener(this);
	}

	public void setScript(GroovyObject script){
		this.script = script;
	}

	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("", FileLocator.class);

		setPauseOnLostFocus(false);

		flyCam.setEnabled(false);
		panel.attachTo(true, viewPort);

		ChaseCamera camera = new ChaseCamera(cam, rootNode, inputManager);

		camera.setDefaultDistance(20);
		camera.setMinDistance(3);
		camera.setMaxDistance(60);
		camera.setDragToRotate(true);
		camera.setInvertVerticalAxis(true);
		camera.setSmoothMotion(true);
		camera.setMinVerticalRotation(FastMath.DEG_TO_RAD * 20);
		camera.setMaxVerticalRotation(FastMath.DEG_TO_RAD * 80);
		camera.setTrailingEnabled(false);
		camera.setChasingSensitivity(50f);
		camera.setDownRotateOnCloseViewOnly(false);
		camera.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 50);

		//camera.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE), new KeyTrigger(KeyInput.KEY_LCONTROL));
	}

	private boolean updateScript(float tpf){
		try{
			Object result = script.invokeMethod("eventUpdate", tpf);
			if (result instanceof Boolean)
				return (Boolean) result;
		} catch (GroovyRuntimeException e){
			e.printStackTrace();
		}
		return false;
	}

	public void simpleUpdate(float tpf){
		if (script != null && !script.getMetaClass().respondsTo(script, "eventUpdate").isEmpty() && updateScript(tpf))
			script.invokeMethod("init", new Object[0]);
	}

	public AwtPanel getPanel(){
		return panel;
	}

	public void clear(){
		enqueue(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				rootNode.detachAllChildren();
				return null;
			}
		});
	}
}
