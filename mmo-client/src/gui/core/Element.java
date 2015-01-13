package gui.core;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;

public class Element extends Node {
	private String id;

	private Vector2f position;
	private Vector2f size;
	private Vector4f imageCords;
	private Vector4f resizeBorders;

	protected Screen screen;

	private ElementContainer parent;

	private ElementMesh model;
	private Geometry geometry;
	private Material material;

	public Element(Vector2f position, Vector2f size, Vector4f imageCords, Vector4f resizeBorders){
		this.position = position;
		this.size = size;
		this.imageCords = imageCords;
		this.resizeBorders = resizeBorders;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Screen getScreen() {
		return screen;
	}

	public void initialize(Screen screen){
		if (this.screen == screen)
			return;

		this.screen = screen;

		createMesh();
		createMaterial();
		createGeometry();

		setImageOffset(V.f(imageCords.x, imageCords.y));

		attachChild(geometry);
		setQueueBucket(RenderQueue.Bucket.Gui);
		setPosition(position);
	}

	private void createMaterial(){
		material = screen.createElementMaterial();
	}

	private void createMesh(){
		Texture atlas = screen.getAtlas();
		Vector2f atlasSize = new Vector2f(atlas.getImage().getWidth(), atlas.getImage().getHeight());

		model = new ElementMesh(size, resizeBorders, atlasSize, V.f(imageCords.z, imageCords.w));
	}

	public void setElementParent(ElementContainer parent) {
		this.parent = parent;
	}

	public ElementContainer getElementParent(){
		return parent;
	}

	private void createGeometry(){
		geometry = new Geometry();
		geometry.setMesh(model);
		geometry.setCullHint(CullHint.Never);
		geometry.setQueueBucket(RenderQueue.Bucket.Gui);
		geometry.setMaterial(material);
	}

	public void setImageOffset(Vector2f imageCords){
		material.setVector2("AtlasOffset", getAtlasCords(imageCords));
	}

	public void setBlendImageOffset(Vector2f imageCords){
		material.setVector2("SecondAtlasOffset", getAtlasCords(imageCords));
	}

	public void enableBlend(boolean enable){
		material.setBoolean("Blend", enable);
	}

	public void setBlendStep(float step){
		material.setFloat("BlendStep", step);
	}

	public Vector2f getPosition() {
		return new Vector2f(position);
	}

	public void setPosition(Vector2f position) {
		this.position.set(position);

		Element parent = getElementParent();

		Vector2f translation = new Vector2f(position.x, position.y);

		/*while (parent != null) {
			if (parent == screen.getElementContainer())
				break;

			translation.subtractLocal(parent.position.x, -parent.position.y);

			parent = parent.getElementParent();
		}

		System.out.println("lilo " + this + " " + translation);*/

		setLocalTranslation(translation.x, translation.y, parent != null ? parent.getLocalTranslation().z - 1 : 0);
		System.out.println(this + " " + getLocalTranslation());
	}

	public Vector2f getSize() {
		return new Vector2f(size);
	}

	public void setSize(Vector2f size) {
		if (this.size.equals(size))
			return;

		this.size.set(size);
		model.updateMeshSize(size);
		geometry.updateModelBound();
	}

	public Vector4f bounds(){
		return V.f(position.x, position.y, position.x + size.x, position.y + size.y);
	}

	public boolean contains(Vector2f point){
		Vector2f position = V.f(getWorldTranslation().x, getWorldTranslation().y);

		return point.x >= position.x &&
			   point.y >= position.y &&
			   point.x <= position.x + size.x &&
			   point.y <= position.y + size.y;
	}

	public void update(float tpf){}

	private Vector2f getAtlasCords(Vector2f imageCords){
		Texture atlas = screen.getAtlas();
		Vector2f atlasSize = new Vector2f(atlas.getImage().getWidth(), atlas.getImage().getHeight());

		// This code is a flippin ninja (texture is flipped left 2 right)
		imageCords = imageCords.clone();
		imageCords.y = atlasSize.y - imageCords.y - this.imageCords.z;

		return V.f(imageCords.x / atlasSize.x, imageCords.y / atlasSize.y);
	}
}
