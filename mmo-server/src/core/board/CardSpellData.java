package core.board;

import groovy.lang.Binding;
import groovy.lang.Script;
import program.main.Program;
import program.main.data.DataLoaderKey;

public class CardSpellData {
	@DataLoaderKey
	private String id;

	@DataLoaderKey("script")
	private String scriptName;

	public String getId() {
		return id;
	}

	public Script compileScript(Binding binding){
		try {
			return Program.getInstance().getScriptEngine().createScript(scriptName, binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
