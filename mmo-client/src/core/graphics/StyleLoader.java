package core.graphics;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.Styles;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class StyleLoader {
	private Compilable api;
	private ScriptEngine engine;
	private Bindings binding;

	public StyleLoader(String apiScript) {
		ScriptEngineManager factory = new ScriptEngineManager();

		this.engine = factory.getEngineByName("groovy");
		this.binding = engine.createBindings();

		binding.put("styles", GuiGlobals.getInstance().getStyles());
		binding.put("gui", GuiGlobals.getInstance());

		compileApi(apiScript);
	}

	private void compileApi(String apiScript) {
		try {
			engine.eval(new FileReader(new File(apiScript)), binding);

			//api = engine.createScript(apiScript, binding);
			//api.run();
		} catch (Exception e) {
			throw new RuntimeException("Error compiling script:" + apiScript, e);
		}
	}

	public void loadStyle(String script) {
		try {
			engine.eval(new FileReader(new File(script)), binding);
/*
			System.out.println(binding.getVariables().size());
			Script style = engine.createScript(script, binding);
			style.run();*/
		} catch (Exception e) {
			throw new RuntimeException("Error running resource:" + script, e);
		}
	}
}
