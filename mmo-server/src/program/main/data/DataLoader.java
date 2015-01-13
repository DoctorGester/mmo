package program.main.data;

import core.main.CardMaster;
import core.main.Npc;
import core.main.PathingMap;
import groovy.util.GroovyScriptEngine;
import program.main.Program;
import program.main.database.entities.CardMasterEntity;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author doc
 */
public class DataLoader {
	private Program program;

	public DataLoader(){
		program = Program.getInstance();
	}

    private Field findDataFieldWithName(Object from, String name){
        for (Field field: from.getClass().getDeclaredFields()){
            DataLoaderKey dataLoaderKey = field.getAnnotation(DataLoaderKey.class);

            if (dataLoaderKey != null){
                String value = dataLoaderKey.value();
                if (value.isEmpty())
                    value = field.getName();

                if (name.equalsIgnoreCase(value))
                    return field;
            }
        }

        return null;
    }

    public void loadFromStream(Object to, InputStream from) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(from));

        Pattern keyStringPattern = Pattern.compile("([a-zA-Z]*)\\s*:\\s*(.*)");

        for(String s = ""; s != null; s = reader.readLine()){
            Matcher keyStringMatcher = keyStringPattern.matcher(s);

            if (keyStringMatcher.matches()){
                String key = keyStringMatcher.group(1),
                       value = keyStringMatcher.group(2);

                Field toWrite = findDataFieldWithName(to, key);

                if (toWrite == null)
                    continue;

				DataLoaderKey dataLoaderKey = toWrite.getAnnotation(DataLoaderKey.class);

                Class<?> type = toWrite.getType();

				if (!dataLoaderKey.dataEnum().equals(Enum.class))
					type = dataLoaderKey.dataEnum();

				Object converted = value;

                if (type == int.class) {
                    converted = Integer.valueOf(value);
                } else if (type == boolean.class) {
					converted = Boolean.valueOf(value);
                } else if (type == double.class) {
					converted = Double.valueOf(value);
                } else if (type == float.class) {
					converted = Float.valueOf(value);
                } else if (Enum.class.isAssignableFrom(type)) {
					converted = Enum.valueOf(dataLoaderKey.dataEnum(), value);
                }

				if (dataLoaderKey.function().isEmpty()){
					if (!toWrite.isAccessible())
						toWrite.setAccessible(true);

					toWrite.set(to, converted);
				} else {
					Method method = to.getClass().getDeclaredMethod(dataLoaderKey.function(), converted.getClass());

					if (!method.isAccessible())
						method.setAccessible(true);

					method.invoke(to, converted);
				}
            }
        }

        from.close();
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

	// Might wanna rework this
	public <T, N> void loadDataList(String file, Class<T> idClass, Class<N> dataClass, Map<T, N> dataMap){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			for(String path = ""; path != null; path = reader.readLine()){
				File data = new File(path);
				if (!data.exists())
					continue;

				N instance = dataClass.newInstance();
				loadFromStream(instance, new FileInputStream(data));

				T id = idClass.cast(instance.getClass().getMethod("getId").invoke(instance));

				dataMap.put(id, instance);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
