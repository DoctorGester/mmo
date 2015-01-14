package gui.core;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;

public class V {
	public static Vector2f f(float x, float y){
		return new Vector2f(x, y);
	}

	public static Vector3f f(float x, float y, float z){
		return new Vector3f(x, y, z);
	}

	public static Vector4f f(float x, float y, float z, float w){
		return new Vector4f(x, y, z, w);
	}

	public static Vector2f f(double x, double y){
		return new Vector2f((float) x, (float) y);
	}
}
