package core.ui;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class ScriptableUIState extends AbstractUIState {
	private static final String SCRIPT_ERROR = "Script execution error in %s on line %d: %s";

	protected GroovyObject script;

	private String scriptName;
	private File scriptFile;
	private long scriptFileModified;

	public ScriptableUIState(String scriptName){
		super();

		this.scriptName = scriptName;

		try {
			scriptFile = new File("res/ui/scripts/" + scriptName);

			if (scriptFile.exists()) {
				reloadScript();

				scriptFileModified = scriptFile.lastModified();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		callScript("create");
	}

	@Override
	public void update(float tpf){
		if (script != null){
			long modified = scriptFile.lastModified();

			if (modified != scriptFileModified){
				scriptFileModified = modified;
				try {
					reloadScript();
					liveReload();
				} catch (CompilationFailedException e) {
					Logger.getLogger(ScriptableUIState.class.getName()).warning(e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void cleanup() {
		super.cleanup();

		callScript("clean");
	}

	public void liveReload(){
		callScript("clean");
		callScript("create");
	}

	public void callScript(String methodName, Object ... arguments){
		List<Object> args = new ArrayList<Object>(Arrays.asList(arguments));
		args.add(0, this);

		if (script.getMetaClass().respondsTo(script, methodName, args.toArray()).isEmpty())
			return;

		try {
			script.invokeMethod(methodName, args.toArray());
		} catch (Exception e) {
			int line = -1;

			StackTraceElement stack[] = e.getStackTrace();

			for (StackTraceElement element: stack){
				if (element.getClassName().equals(script.getMetaClass().getTheClass().getName()))
					line = element.getLineNumber();
			}

			Logger.getLogger(ScriptableUIState.class.getName()).warning(String.format(SCRIPT_ERROR, scriptName, line, e.getMessage()));
		}
	}

	private void reloadScript() throws Exception {
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		Class groovyClass = loader.parseClass(scriptFile);

		script = (GroovyObject) groovyClass.newInstance();
	}
}
