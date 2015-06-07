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
public abstract class CardModel extends Node {
	protected Geometry base;

	public CardModel(AssetManager manager, float size){
		super();

		Mesh mesh = new CardMesh(size * 0.67f, size);
		base = new Geometry("Card", mesh);

		Material material = new Material(manager, "res/shaders/Card.j3md");

		material.setTexture("Base", manager.loadTexture("res/textures/card.png"));
		material.setTexture("Mask", manager.loadTexture("res/textures/card_mask.png"));
		material.setTexture("Portrait", manager.loadTexture("res/textures/card_portrait.png"));
		material.setTexture("Content", manager.loadTexture("res/textures/card_content.png"));

		base.setMaterial(material);

		attachChild(base);
	}

	public abstract void createContent();
}
