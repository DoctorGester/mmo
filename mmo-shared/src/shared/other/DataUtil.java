package shared.other;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FileUtils;
import shared.board.Cell;
import shared.board.Unit;
import shared.board.data.*;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	public static String dump(Object object) {
		Field[] fields = object.getClass().getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		sb.append(object.getClass().getSimpleName()).append('{');

		boolean firstRound = true;

		for (Field field : fields) {
			if (!firstRound) {
				sb.append(", ");
			}
			firstRound = false;
			field.setAccessible(true);
			try {
				final Object fieldObj = field.get(object);
				final String value;
				if (null == fieldObj) {
					value = "null";
				} else {
					value = fieldObj.toString();
				}
				sb.append(field.getName()).append('=').append('\'')
						.append(value).append('\'');
			} catch (IllegalAccessException ignore) {
				//this should never happen
			}
		}

		sb.append('}');
		return sb.toString();
	}

	/**
	 * First bit of the first byte is gonna be marked
	 * @param mark either 0 or 1
	 * @param number number to convert, limited to abs(Integer.MAX_VALUE)
	 * @return converted value
	 */
	public static byte[] markedIntToByte(int mark, int number){
		byte first = (byte) ((number >> 24) & 0xff);
		if (mark == 1)
			first |= 0x80; // Setting first bit to 1
		return new byte[] {
				first,
				(byte)((number >> 16) & 0xff),
				(byte)((number >> 8) & 0xff),
				(byte)(number & 0xff),
		};
	}

	public static int[] byteToMarkedInt(byte data[]){
		int result[] = new int[2];
		// If first bit is not zero setting it to 0, putting mark
		if ((data[0] & 0x80) != 0){
			result[0] = 1;
			data[0] &= 0x7f;
		}
		result[1] = byteToInt(data);
		return result;
	}

	public static byte[] intToVarInt(int ... number){
		int all = 0;
		byte data[][] = new byte[number.length][5];
		for(int i = 0; i < number.length; i++){
			int pos = 0;
			while(true){
				if ((number[i] & ~0x7f) == 0){
					data[i][pos] = (byte) number[i];
					data[i] = Arrays.copyOf(data[i], pos + 1);
					all += data[i].length;
					break;
				} else {
					data[i][pos++] = (byte) ((number[i] & 0x7F) | 0x80);
					number[i] >>>= 7;
				}
			}
		}
		byte result[] = new byte[all];

		for (int i = 0, pos = 0; i < number.length; i++){
			System.arraycopy(data[i], 0, result, pos, data[i].length);
			pos += data[i].length;
		}

		return result;
	}

	public static int[] varIntsToInts(byte data[]){
		int result[] = new int[128], resultNum = 0, pos = 0;
		for(byte b: data){
			result[resultNum] |= (b & 0x7f) << (pos * 7);
			pos++;

			if ((b & ~0x7f) == 0){
				resultNum++;
				pos = 0;
			}
		}
		return Arrays.copyOf(result, resultNum);
	}

	public static byte[] shortToByte(short s){
		return new byte[] {
	        (byte)((s >> 8) & 0xff),
	        (byte)((s) & 0xff),
	    };
	}
	
	public static byte[] intToByte(int i){
		return new byte[] {
	        (byte)((i >> 24) & 0xff),
	        (byte)((i >> 16) & 0xff),
	        (byte)((i >> 8) & 0xff),
	        (byte)((i) & 0xff),
	    };
	}
	
	public static byte[] floatToByte(float f){
		return intToByte(Float.floatToRawIntBits(f));
	}
	
	public static byte[] boolToByte(boolean ... bools){
		byte bytes[] = new byte[bools.length / 8];
		for(int i = 0; i < bytes.length; i++)
			for(int j = 0; j < 8; j++){
				bytes[i] <<= 1;
				if (bools[i * 8 + j])
					bytes[i] |= 1;
			}
		return bytes;
	}
	
	public static boolean[] byteToBool(byte[] bytes) {
		boolean[] bits = new boolean[bytes.length * 8];
		for (int i = 0; i < bytes.length * 8; i++) 
			bits[i] = ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0);
		return bits;
	}
	
	public static short byteToShort(byte[] data) {
	    return (short)((0xff & data[0]) << 8 | (0xff & data[1]));
	}
	
	public static int byteToInt(byte[] data) {
	    return (0xff & data[0]) << 24 | (0xff & data[1]) << 16 | (0xff & data[2]) << 8 | (0xff & data[3]);
	}
	
	public static float byteToFloat(byte[] data) {
	    return Float.intBitsToFloat(byteToInt(data));
	}
	
	public static void arrayInsert(byte[] arr, byte[] to, int pos){
		System.arraycopy(arr, 0, to, pos, arr.length);
	}
	
	public static void arrayInsert(byte[] arr, byte[] to){
		System.arraycopy(arr, 0, to, arrCounter, arr.length);
		arrCounter += arr.length;
	}
	
	public static void arrayCounterReset(){
		arrCounter = 0;
	}
	
	public static void arrayDetach(byte[] arr, byte[] to, int pos){
		System.arraycopy(arr, 0, to, pos, to.length);
	}
	
	public static void arrayDetach(byte[] arr, byte[] to){
		System.arraycopy(arr, arrCounter, to, 0, to.length);
		arrCounter += to.length;
	}

	private static volatile int arrCounter = 0;

	public static Vector2 slerp(Vector2 from, Vector2 to, float step){
		if (step == 0) return from;
		if (from == to || step == 1) return to;

		float theta = (float) Math.acos(from.dot(to));
		if (theta == 0) return to;

		float sinTheta = (float) Math.sin(theta);

		Vector2 fromValue = from.clone().multLocal((float) Math.sin((1 - step) * theta) / sinTheta),
				 toValue = to.clone().multLocal((float) Math.sin(step * theta) / sinTheta);

		return fromValue.add(toValue);
	}

	public static <T> T[] concatenate(T[] first, T[] second, Class<T[]> type){
		T[] result = type.cast(Array.newInstance(type, first.length + second.length));
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);

		return result;
	}

	public static byte[] concatenate(byte[] first, byte[] second){
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);

		return result;
	}

	public static DataInputStream stream(byte data[]){
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
		return new DataInputStream(byteInputStream);
	}


	private static Field findDataFieldWithName(Object from, String name){
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

	public static void loadFromStream(Object to, InputStream from) throws Exception{
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

	// Might wanna rework this
	public static <T, N> void loadDataList(String file, Class<T> idClass, Class<N> dataClass, Map<T, N> dataMap){
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

	public static <T extends DataElement> Map<String, T> loadDataList(String file, Class<T> type) throws IOException {
		String in = FileUtils.readFileToString(new File(file));
		JsonReader reader = new JsonReader(new StringReader(in));

		return new DataAdapter<T>(type).read(reader);
	}

	public static <T extends DataElement> void saveDataList(String file, Map<String, T> data, Class<T> type) throws IOException {
		StringWriter out = new StringWriter();

		JsonWriter writer = new JsonWriter(out);
		writer.setSerializeNulls(true);
		writer.setIndent("  ");

		new DataAdapter<T>(type).write(writer, data);

		FileUtils.writeStringToFile(new File(file), out.toString());
	}

	public static int distance(Cell from, Cell to){
		return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
	}

	public static int distance(Unit from, Unit to){
		return Math.abs(from.getPosition().getX() - to.getPosition().getX()) + Math.abs(from.getPosition().getY() - to.getPosition().getY());
	}
}
