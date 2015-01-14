package core.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import core.main.ClientCardMaster;
import program.main.Program;
import shared.map.CardMaster;

public class CardMasterSpatial {
	private Node node;
	private Spatial spatial;
	private Geometry mainGeometry;
	private ClientCardMaster cardMaster;

	private Node nameNode;
	private String name = "";
	private BitmapText nameBitmap;

	private AnimControl animControl;
	private AnimChannel animChannel;

	public CardMasterSpatial(Spatial spatial, ClientCardMaster cardMaster){
		this.node = new Node("CardMaster");
		this.cardMaster = cardMaster;
		this.spatial = spatial;
		this.node.attachChild(spatial);

		animControl = spatial.getControl(AnimControl.class);
		animChannel = animControl.createChannel();

		//SkeletonControl skeletonControl = spatial.getControl(SkeletonControl.class);
		//skeletonControl.setHardwareSkinningPreferred(true);

		Node node = (Node) spatial;

		for(Spatial child: node.getChildren()){
			if (child instanceof Geometry){
				mainGeometry = (Geometry) child;
				break;
			}
		}
	}

	public AnimChannel getAnimChannel(){
		return animChannel;
	}

	public Node getNode(){
		return node;
	}

	public Spatial getSpatial(){
		return spatial;
	}

	public ClientCardMaster getCardMaster(){
		return cardMaster;
	}

	public String getName(){
		return name;
	}

	public Geometry getGeometry(){
		return mainGeometry;
	}

	public void setName(String name){
		this.name = name;

		if (nameBitmap == null){
			nameBitmap = new BitmapText(Program.getInstance().getMainFrame().getOutlinedFont(), false);
			nameBitmap.setColor(new ColorRGBA(1f, 0.0f, 0.0f, 1f));
			nameBitmap.setSize(1f);
			nameBitmap.setQueueBucket(RenderQueue.Bucket.Transparent);

			nameNode = new Node();
			nameNode.addControl(new BillboardControl());
			nameNode.attachChild(nameBitmap);
			nameNode.setLocalTranslation(new Vector3f(0, 7f, 0f));
			node.attachChild(nameNode);
		}

		nameBitmap.setText(name);
		nameBitmap.setLocalTranslation(-nameBitmap.getLineWidth() * 0.5f, 0f, 0f);
	}
}
