package core.graphics;

import com.simsilica.lemur.GuiGlobals;
import groovy.lang.GroovyShell;

import java.io.File;

public class StyleLoader {
	private GroovyShell shell;

	public StyleLoader(String apiScript) {
		shell = new GroovyShell();
		shell.setProperty("styles", GuiGlobals.getInstance().getStyles());
		shell.setProperty("gui", GuiGlobals.getInstance());
	}

	public void loadStyle(String script) {
		try {
			shell.evaluate(new File(script));
		} catch (Exception e) {
			throw new RuntimeException("Error running resource:" + script, e);
		}
	}
}
