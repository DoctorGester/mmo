package program.main.data;

import core.main.PathingMap;
import groovy.util.GroovyScriptEngine;
import program.main.Program;
import shared.other.DataLoaderKey;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author doc
 */
public class ServerDataLoader {
	private Program program;

	public ServerDataLoader(){
		program = Program.getInstance();
	}

	public GroovyScriptEngine loadScriptEngine(){
		try {
			String[] roots = new String[] {
					"res/units/scripts",
					"res/spells/unit/scripts",
					"res/spells/hero/scripts",
					"res/buffs/scripts"
			};
			return new GroovyScriptEngine(roots);
		} catch (IOException e) {
			e.printStackTrace();
		}

        return null;
	}

	public void loadPathingMapFromFileSystem(){
		try {
			FileInputStream fis = new FileInputStream("res/map.map");
			byte data[] = new byte[fis.available()];
			int red = fis.read(data);
			fis.close();
			int size = (int) Math.sqrt(red * 8);
			program.setPathingMap(new PathingMap(data, size, size));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
