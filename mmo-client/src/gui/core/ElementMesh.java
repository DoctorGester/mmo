package gui.core;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author t0neg0d
 * @author doc
 */
public class ElementMesh extends Mesh {
	private FloatBuffer verts = BufferUtils.createFloatBuffer(16 * 3);
	private FloatBuffer coords = BufferUtils.createFloatBuffer(16 * 2);
	private ShortBuffer indices = BufferUtils.createShortBuffer(3 * 3 * 6);
	private FloatBuffer normals = BufferUtils.createFloatBuffer(16 * 3);

	private Vector4f borders;

	public ElementMesh(Vector2f size, Vector4f borders, Vector2f atlasSize, Vector2f textureSize) {
		this.borders = borders;

		updateIndices();
		updateNormals();
		updateMeshSize(size);
		updateTextureSize(textureSize, atlasSize);
	}

	private void updateNormals() {
		final float[] baseNormals = new float[]{ 0f, 0f, -1f };

		for (int x = 0; x < normals.capacity(); x += 3) {
			normals.put(x, baseNormals[0]);
			normals.put(x + 1, baseNormals[1]);
			normals.put(x + 2, baseNormals[2]);
		}

		clearBuffer(Type.Normal);
		setBuffer(Type.Normal, 3, normals);
	}

	private void updateIndices() {
		// I don't even have a slightest clue about how it works, props to original author
		final short[] baseIndices = new short[]{ 0, 4, 5, 5, 1, 0 };

		for (int i = 0, mod = 0; i < indices.capacity(); i++, mod = i % 18){
			int baseIndex = mod % 6;
			int indexX = mod / 6 + i / 18 * 4;

			indices.put(i, (short) (baseIndices[baseIndex] + indexX));
		}

		clearBuffer(Type.Index);
		setBuffer(Type.Index, 3, indices);
	}

	public void updateTextureSize(Vector2f textureSize, Vector2f atlasSize) {
		float width = textureSize.x / atlasSize.x;
		float height = textureSize.y / atlasSize.y;

		float baseX[] = new float[]{
				0, borders.y / atlasSize.x, height - (borders.z / atlasSize.x), height
		};
		float baseY[] = new float[]{
				0, borders.x / atlasSize.y, width - (borders.w / atlasSize.y), width
		};

		int index = 0, indexX = 0, indexY = 0;
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				coords.put(index++, baseX[indexX]);
				coords.put(index++, baseY[indexY]);
				indexX = (indexX + 1) % 4;
			}
			indexY++;
		}

		clearBuffer(Type.TexCoord);
		setBuffer(Type.TexCoord, 2, coords);
	}

	public void updateMeshSize(Vector2f size) {
		float baseX[] = new float[]{
				0f, borders.y, size.x - borders.z, size.x
		};
		float baseY[] = new float[]{
				0f, borders.x, size.y - borders.w, size.y
		};

		int index = 0, indexX = 0, indexY = 0;
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				verts.put(index++, baseX[indexX]);
				verts.put(index++, baseY[indexY]);
				verts.put(index++, 0);

				indexX = (indexX + 1) % 4;
			}
			indexY++;
		}

		clearBuffer(Type.Position);
		setBuffer(Type.Position, 3, verts);

		createCollisionData();
		updateBound();
	}
}