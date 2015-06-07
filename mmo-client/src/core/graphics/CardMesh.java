package core.graphics;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

public class CardMesh extends Mesh {
	private float width;
	private float height;

	public CardMesh(float width, float height){
		updateGeometry(width, height);
	}

	public float getHeight() {
		return height;
	}

	public float getWidth() {
		return width;
	}

	// Two sided quad with it's centre in 0;0
	public void updateGeometry(float width, float height) {
		this.width = width;
		this.height = height;

		float w = width / 2f,
			  h = height / 2f;

		setBuffer(VertexBuffer.Type.Position, 3, new float[]{-w, -h, 0,
															  w, -h, 0,
															  w,  h, 0,
															 -w,  h, 0,
		// MORE POLYGONS FOR THE GOD OF POLYGONS
															 -w, -h, 0,
															  w, -h, 0,
															  w,  h, 0,
															 -w,  h, 0,
		});


		setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{0.5f, 1,
															 1, 1,
															 1, 0,
															 0.5f, 0,

															 0.5f, 0,
															 0, 0,
															 0, 1,
															 0.5f, 1,
		});

		setBuffer(VertexBuffer.Type.Normal, 3, new float[]{0, 0, 1,
														   0, 0, 1,
														   0, 0, 1,
														   0, 0, 1,

														   0, 0, -1,
														   0, 0, -1,
														   0, 0, -1,
														   0, 0, -1,
		});

		setBuffer(VertexBuffer.Type.Index, 3, new short[]{0, 1, 2,
														  0, 2, 3,
														  6, 5, 4,
														  7, 6, 4
		});

		updateBound();
	}
}
