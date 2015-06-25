package core.ui.buffers;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class UIBuffer extends RenderingBuffer {
    public UIBuffer(RenderManager renderManager, int width, int height) {
        super(renderManager, width, height);
    }

    @Override
    public void setupCamera(Camera camera) {
        camera.setFrustumFar(36000f);
        float aspect = (float) camera.getWidth() / (float) camera.getHeight();
        camera.setFrustumPerspective(45f, aspect, 0.1f, camera.getFrustumFar());
    }

    @Override
    public void setupRoot(Node root) {
        root.setQueueBucket(RenderQueue.Bucket.Gui);
        root.setCullHint(Spatial.CullHint.Never);
    }

    @Override
    public ViewPort createViewPort(RenderManager renderManager, Camera camera){
        ViewPort viewPort = renderManager.createPostView("UI buffer", camera);
        viewPort.setClearFlags(false, false, false);

        return viewPort;
    }
}
