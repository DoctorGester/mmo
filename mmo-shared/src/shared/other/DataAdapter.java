package shared.other;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doc
 */
public class DataAdapter<T extends DataElement> extends TypeAdapter<Map<String, T>> {
	private Logger log = LoggerFactory.getLogger(DataAdapter.class);

	private Class<T> type;

	public DataAdapter(Class<T> type) {
		this.type = type;
	}

	@Override
	public void write(JsonWriter out, Map<String, T> data) throws IOException {
		out.beginObject();

		for (Map.Entry<String, T> entry : data.entrySet()) {
			out.name(entry.getKey());
			out.beginObject();

			T instance = entry.getValue();

			for (Field field : type.getDeclaredFields()) {
				try {
					DataLoaderKey dataLoaderKey = field.getAnnotation(DataLoaderKey.class);

					if (dataLoaderKey != null) {
						String name = field.getName();
						Class<?> type = getFieldClass(field);

						out.name(name);

						if (!field.isAccessible())
							field.setAccessible(true);

						if (Collection.class.isAssignableFrom(type)) {
							out.beginArray();

							Collection cast = Collection.class.cast(field.get(instance));

							if (cast != null) {
								for (Object object : cast) {
									putTypedObject(out, object, dataLoaderKey);
								}
							}

							out.endArray();
						} else {
							putTypedObject(out, field.get(instance), dataLoaderKey);
						}
					}
				} catch (Exception e) {
					log.error("Error parsing the field", e);
				}
			}

			out.endObject();
		}

		out.endObject();
	}

	@Override
	public Map<String, T> read(JsonReader in) throws IOException {
		Map<String, T> result = new HashMap<String, T>();

		in.beginObject();

		while(true) {
			try {
				String id = in.nextName();
				T instance = type.newInstance();

				in.beginObject();

				while (true) {
					String property = in.nextName();
					Field field = findDataFieldWithName(instance, property);
					Class<?> type = getFieldClass(field);
					DataLoaderKey dataLoaderKey = field.getAnnotation(DataLoaderKey.class);

					if (Collection.class.isAssignableFrom(type)) {
						in.beginArray();

						while (in.peek() != JsonToken.END_ARRAY) {
							Object converted = getTypedObject(in, field, dataLoaderKey);

							if (converted == null) {
								continue;
							}

							writeObject(field, dataLoaderKey, instance, converted);
						}

						in.endArray();

					} else {
						Object converted = getTypedObject(in, field, dataLoaderKey);

						if (converted == null) {
							in.skipValue();
							continue;
						}

						writeObject(field, dataLoaderKey, instance, converted);
					}

					if (in.peek() == JsonToken.END_OBJECT) {
						break;
					}

				}

				result.put(id, instance);
				in.endObject();

				if (in.peek() == JsonToken.END_OBJECT)
					break;

			} catch (Exception e) {
				log.error("Error reading json", e);
			}
		}

		in.endObject();

		return result;
	}

	private void writeObject(Field field, DataLoaderKey dataLoaderKey, T instance, Object object){
		try {
			if (dataLoaderKey.function().isEmpty()) {
				if (!field.isAccessible())
					field.setAccessible(true);

				field.set(instance, object);
			} else {
				Method method = type.getDeclaredMethod(dataLoaderKey.function(), object.getClass());

				if (!method.isAccessible())
					method.setAccessible(true);

				method.invoke(instance, object);
			}
		} catch (Exception e) {
			log.error("Error writing to field " + field.getName(), e);
		}
	}

	private Object getTypedObject(JsonReader in, Field field, DataLoaderKey dataLoaderKey) throws Exception {
		Class<?> type = getFieldClass(field);
		Object converted = null;

		if (!dataLoaderKey.dataEnum().equals(Enum.class))
			type = dataLoaderKey.dataEnum();

		if (type == Integer.class || type == int.class) {
			converted = in.nextInt();
		} else if (type == Boolean.class || type == boolean.class) {
			converted = in.nextBoolean();
		} else if (type == Double.class || type == Float.class || type == double.class || type == float.class) {
			converted = (float) in.nextDouble();
		} else if (type == String.class) {
			converted = in.nextString();
		} else if (Enum.class.isAssignableFrom(type)) {
			converted = Enum.valueOf(dataLoaderKey.dataEnum(), in.nextString());
		}

		return converted;
	}

	private void putTypedObject(JsonWriter out, Object value, DataLoaderKey dataLoaderKey) {
		try {
			if (value == null){
				out.nullValue();
				return;
			}

			Class<?> type = value.getClass();

			if (type == Integer.class) {
				out.value((Integer) value);
			} else if (type == Boolean.class) {
				out.value((Boolean) value);
			} else if (type == Double.class) {
				out.value((Double) value);
			} else if (type == Float.class) {
				out.value((Float) value);
			} else if (type == String.class) {
				out.value((String) value);
			} else if (Enum.class.isAssignableFrom(type)) {
				Enum cast = dataLoaderKey.dataEnum().cast(value);
				out.value(cast.toString());
			} else {
				out.nullValue();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Field findDataFieldWithName(T from, String name) {
		for (Field field : from.getClass().getDeclaredFields()) {
			DataLoaderKey dataLoaderKey = field.getAnnotation(DataLoaderKey.class);

			if (dataLoaderKey != null) {
				String value = field.getName();

				if (name.equalsIgnoreCase(value))
					return field;
			}
		}

		return null;
	}

	private Class<?> getFieldClass(Field field) throws ClassNotFoundException {
		if (field.getType().isPrimitive())
			return field.getType();

		Type genericType = field.getGenericType();

		if (genericType instanceof ParameterizedType){
			return getClass(((ParameterizedType) genericType).getRawType());
		}

		return getClass(genericType);
	}

	private static Class<?> getClass(Type type) throws ClassNotFoundException {
		final String prefixes[] = new String[] { "class ", "interface " };
		String fullName = type.toString();

		for (String prefix: prefixes)
			if (fullName.startsWith(prefix))
				return Class.forName(fullName.substring(prefix.length()));

		return Class.forName(fullName);
	}
}
