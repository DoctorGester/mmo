package core.graphics;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.UpdateControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;

import java.util.concurrent.*;

/**
 * @author doc
 */
public class TerrainPager extends Node {
	private TerrainCache cache;
	private AssetManager assetManager;
	private int size;
	private int patchSize;
	private Vector2f currentPosition;
	private Material material;
	private int lineOfSight = 1;

	public TerrainPager(SimpleApplication app, int size, int patchSize){
		this.assetManager = app.getAssetManager();
		this.size = size;
		this.patchSize = patchSize;

		cache = new TerrainCache(16, this);

		addControl(new UpdateControl());
	}

	public int getLineOfSight() {
		return lineOfSight;
	}

	public void setLineOfSight(int lineOfSight) {
		this.lineOfSight = lineOfSight;
		lineOfSight *= 3;
		if (lineOfSight * lineOfSight > cache.getCacheSize())
			setCacheSize(lineOfSight * lineOfSight);
	}

	public void setCacheSize(int size){
		cache.setCacheSize(size);
	}

	private Vector2f locationToSector(Vector3f location){
		Vector3f divided = location.divide(size);
		return new Vector2f((int) FastMath.floor(divided.x + 0.5f), (int) FastMath.floor(divided.z + 0.5f));
	}

	public float getHeight(Vector2f at){
		Vector2f divided = at.divide(size);
		Vector2f sector = new Vector2f(FastMath.floor(divided.x + 0.5f), FastMath.floor(divided.y + 0.5f));

		TerrainQuad quad = cache.get(sector);
		if (quad == null){
			try {
				updateAt((int) sector.x, (int) sector.y).get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			quad = cache.get(sector);
		}

		return quad.getHeight(at);
	}

	private TerrainQuad getTerrainAt(Vector2f position){
		String name = "res/map/map-" + (int) position.y + "-" + (int) position.x + ".png";
		HeightMap heightMapAt = null;
		try {
			Texture texture = assetManager.loadTexture(new TextureKey(name));
			heightMapAt = new ImageBasedHeightMap(texture.getImage());
			heightMapAt.load();
		} catch (AssetNotFoundException e) {
			System.err.println(String.format("Asset %s not found, loading zero heightmap instead", name));
		}
		return new TerrainQuad("Quad" + position, patchSize, size, heightMapAt == null ? null : heightMapAt.getHeightMap());
	}

	private Future<?> updateAt(int x, int y){
		return getControl(UpdateControl.class).enqueue(new UpdateQuads(new Vector2f(x, y)));
	}

	private void updateAtRelated(int x, int y){
		getControl(UpdateControl.class).enqueue(new UpdateQuads(currentPosition.clone().addLocal(x, y)));
	}

	public void updateFromCamera(Camera camera){
		update(camera.getLocation());
	}

	public void update(Vector3f from){
		Vector2f currentPosition = locationToSector(from);
		if (currentPosition.equals(this.currentPosition))
			return;

		if (this.currentPosition == null)
			this.currentPosition = new Vector2f(currentPosition);
		else
			this.currentPosition.set(currentPosition);

		for (int round = 0; round <= lineOfSight; round++)
			for (int x = -round; x <= round; x++)
				for (int y = -round; y <= round; y++)
					if (Math.abs(x) == round || Math.abs(y) == round)
						updateAtRelated(x, y);
	}

	public void setMaterial(Material material){
		this.material = material;
		super.setMaterial(material);
	}

	private class UpdateQuads implements Callable<Void>{

		private Vector2f at;

		public UpdateQuads(Vector2f at){
			this.at = at;
		}

		@Override
		public Void call() {
			TerrainQuad quad = cache.get(at);
			if (quad != null)
				return null;

			Vector2f m = at.mult(size - 2);
			quad = getTerrainAt(at);
			quad.setMaterial(material);
			quad.setLocalTranslation(new Vector3f(m.x, 0, m.y));
			quad.setShadowMode(RenderQueue.ShadowMode.Receive);
			cache.put(at, quad);

			attachChild(quad);

			return null;
		}
	}
}
