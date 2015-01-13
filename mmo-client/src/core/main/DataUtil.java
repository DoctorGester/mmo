package core.main;

import com.jme3.math.Vector2f;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.StringTokenizer;

public class DataUtil {
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

	public static Vector2f slerp(Vector2f from, Vector2f to, float step){
		if (step == 0) return from;
		if (from == to || step == 1) return to;

		float theta = (float) Math.acos(from.dot(to));
		if (theta == 0) return to;

		float sinTheta = (float) Math.sin(theta);

		Vector2f fromValue = from.clone().multLocal((float) Math.sin((1 - step) * theta) / sinTheta),
				 toValue = to.clone().multLocal((float) Math.sin(step * theta) / sinTheta);

		return fromValue.add(toValue);
	}

	public static Vector2f parseVector2f(String str, Vector2f top) {
		str = str.replaceAll("\\s", "").trim();
		StringTokenizer st = new StringTokenizer(str, ",");
		float x, y;

		String xComp = st.nextToken();
		String yComp = st.nextToken();

		if (xComp.contains("%"))
			x = top.x * (Float.parseFloat(xComp.substring(0, xComp.indexOf("%"))) * 0.01f);
		else
			x = Float.parseFloat(xComp);
		if (yComp.contains("%"))
			y = top.y * (Float.parseFloat(yComp.substring(0, yComp.indexOf("%"))) * 0.01f);
		else
			y = Float.parseFloat(yComp);

		return new Vector2f(x, y);
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
}
