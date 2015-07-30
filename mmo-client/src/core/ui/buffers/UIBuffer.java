package core.ui.buffers;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class UIBuffer extends RenderingBuffer {
    private Spatial content;

    public UIBuffer(RenderManager renderManager, int width, int height, Spatial content) {
        super(renderManager, width, height);
        this.content = content;
    }

    @Override
    public void setupCamera(Camera camera) {
        float aspect = (float) camera.getWidth() / (float) camera.getHeight();
        camera.setFrustumPerspective(45f, aspect, 0.1f, camera.getFrustumFar());
        camera.setFrustumFar(36000f);
        camera.setLocation(new Vector3f(0, 0, 5));
        camera.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
    }

    @Override
    public void setupRoot(Node root) {
        root.setQueueBucket(RenderQueue.Bucket.Gui);
        root.setCullHint(Spatial.CullHint.Never);

        root.attachChild(content);
    }

    @Override
    public ViewPort createViewPort(RenderManager renderManager, Camera camera){
        ViewPort viewPort = super.createViewPort(renderManager, camera);
        //viewPort.setClearFlags(false, false, false);
        viewPort.setBackgroundColor(new ColorRGBA(1f, 1f, 1f, 0f));

        return viewPort;
    }
}
