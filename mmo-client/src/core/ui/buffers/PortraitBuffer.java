package core.ui.buffers;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import core.ui.PortraitData;

public class PortraitBuffer extends RenderingBuffer {
    private PortraitData portraitData;

    public PortraitBuffer(RenderManager renderManager, PortraitData portraitData) {
        super(renderManager, portraitData.getWidth(), portraitData.getHeight());
        this.portraitData = portraitData;
    }

    @Override
    public ViewPort createViewPort(RenderManager renderManager, Camera camera) {
        ViewPort viewPort = super.createViewPort(renderManager, camera);
        viewPort.setBackgroundColor(ColorRGBA.White);

        return viewPort;
    }

    @Override
    public void setupCamera(Camera camera) {
        float aspect = (float) camera.getWidth() / (float) camera.getHeight();

        camera.setFrustumFar(36000f);
        camera.setFrustumPerspective(45f, aspect, 0.1f, camera.getFrustumFar());
        camera.setLocation(portraitData.getCameraLocation());
        camera.lookAt(portraitData.getCameraTarget(), Vector3f.UNIT_Y);
    }

    @Override
    public void setupRoot(Node root) {
        root.attachChild(portraitData.getScene());
    }
}
