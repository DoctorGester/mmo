package core.ui.deck;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import core.graphics.CardMesh;

/**
 * Created by kartemov on 28.05.2015.
 */
public class CardModel extends Node {
	private Geometry geometry;

	public CardModel(AssetManager manager, float size){
		super();

		Mesh mesh = new CardMesh(size * 0.67f, size);
		geometry = new Geometry("Card", mesh);

		Material material = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture texture = manager.loadTexture("res/textures/card.png");
		material.setTexture("ColorMap", texture);
		geometry.setMaterial(material);

		attachChild(geometry);
	}
}
