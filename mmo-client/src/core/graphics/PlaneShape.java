package core.graphics;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;

public class PlaneShape extends Mesh {
	public final Vector3f center = new Vector3f(0f, 0f, 0f);

	public float xExtent, yExtent;

	private static final short[] GEOMETRY_INDICES_DATA = {
			2, 1, 0, 3, 2, 0, // back
			6, 5, 4, 7, 6, 4, // front
	};

	private static final float[] GEOMETRY_NORMALS_DATA = {
			0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, // back
			0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, // front
	};

	private static final float[] GEOMETRY_TEXTURE_DATA = {
			0, 1, 1, 1, 1, 0, 0, 0, // back
			0, 1, 0, 0, 1, 0, 1, 1, // front
	};

	public PlaneShape(float x, float y) {
		super();
		updateGeometry(Vector3f.ZERO, x, y);
	}

	private void duUpdateGeometryIndices() {
		if (getBuffer(VertexBuffer.Type.Index) == null) {
			setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createShortBuffer(GEOMETRY_INDICES_DATA));
		}
	}

	private void duUpdateGeometryNormals() {
		if (getBuffer(VertexBuffer.Type.Normal) == null) {
			setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(GEOMETRY_NORMALS_DATA));
		}
	}

	private void duUpdateGeometryTextures() {
		if (getBuffer(VertexBuffer.Type.TexCoord) == null) {
			setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(GEOMETRY_TEXTURE_DATA));
		}
	}

	private void duUpdateGeometryVertices() {
		FloatBuffer fpb = BufferUtils.createVector3Buffer(24);
		Vector3f[] v = computeVertices();
		fpb.put(new float[]{
				v[0].x, v[0].y, v[0].z, v[1].x, v[1].y, v[1].z, v[2].x, v[2].y, v[2].z, v[3].x, v[3].y, v[3].z, // back
				v[0].x, v[0].y, v[0].z, v[3].x, v[3].y, v[3].z, v[2].x, v[2].y, v[2].z, v[1].x, v[1].y, v[1].z  // front
		});
		setBuffer(VertexBuffer.Type.Position, 3, fpb);
		updateBound();
	}

	private Vector3f[] computeVertices() {
		Vector3f[] axes = {
				Vector3f.UNIT_X.mult(xExtent),
				Vector3f.UNIT_Y.mult(yExtent)
		};
		return new Vector3f[] {
				center.subtract(axes[0]).subtractLocal(axes[1]),
				center.add(axes[0]).subtractLocal(axes[1]),
				center.add(axes[0]).addLocal(axes[1]),
				center.subtract(axes[0]).addLocal(axes[1]),
		};
	}

	public final void updateGeometry(Vector3f center, float x, float y) {
		if (center != null) { this.center.set(center); }
		this.xExtent = x;
		this.yExtent = y;
		updateGeometry();
	}

	public final void updateGeometry() {
		duUpdateGeometryVertices();
		duUpdateGeometryNormals();
		duUpdateGeometryTextures();
		duUpdateGeometryIndices();
	}
}
