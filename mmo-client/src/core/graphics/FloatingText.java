package core.graphics;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import program.main.Program;

/**
 * @author doc
 */
public class FloatingText {

	private String text = "";
	private Vector3f velocity = new Vector3f();
	private Vector3f location = new Vector3f();
	private Node node = new Node();
	private BitmapText bitmapText;
	private float fadeTime, fadeTimeMax;
	private ColorRGBA color = new ColorRGBA();
	private boolean onDestroy;

	public FloatingText(){
		bitmapText = new BitmapText(Program.getInstance().getMainFrame().getOutlinedFont(), false);
		bitmapText.setColor(new ColorRGBA(1f, 0.0f, 0.0f, 1f));
		bitmapText.setSize(1f);
		bitmapText.setQueueBucket(RenderQueue.Bucket.Transparent);

		node.addControl(new BillboardControl());
		node.attachChild(bitmapText);
		node.setLocalTranslation(new Vector3f(0, 7f, 0f));
	}

	public FloatingText setSize(float size){
		bitmapText.setSize(size);
		return this;
	}

	public String getText() {
		return text;
	}

	public FloatingText setText(String text) {
		this.text = text;

		bitmapText.setText(text);
		bitmapText.setLocalTranslation(-bitmapText.getLineWidth() * 0.5f, 0f, 0f);

		return this;
	}

	public FloatingText setStyle(int style){
		bitmapText.setStyle(".*", style);

		return this;
	}

	public Vector3f getVelocity() {
		return velocity;
	}

	public FloatingText setVelocity(Vector3f velocity) {
		this.velocity = velocity;
		return this;
	}

	public Vector3f getLocation() {
		return location;
	}

	public FloatingText setLocation(Vector3f location) {
		this.location.set(location);
		return this;
	}

	public float getFadeTime() {
		return fadeTimeMax;
	}

	public FloatingText setFadeTime(float fadeTime) {
		this.fadeTime = fadeTime;
		fadeTimeMax = fadeTime;
		return this;
	}

	public ColorRGBA getColor() {
		return color;
	}

	public FloatingText setColor(ColorRGBA color) {
		this.color.set(color);
		bitmapText.setColor(color);
		return this;
	}

	public Node getNode() {
		return node;
	}

	public boolean isOnDestroy() {
		return onDestroy;
	}

	public void update(float tpf){
		if (onDestroy)
			return;

		location.addLocal(velocity.mult(tpf));
		node.setLocalTranslation(location);

		if (fadeTimeMax > 0){
			fadeTime -= tpf;

			bitmapText.setAlpha(fadeTime / fadeTimeMax);

			if (fadeTime <= 0)
				onDestroy = true;
		}
	}
}
