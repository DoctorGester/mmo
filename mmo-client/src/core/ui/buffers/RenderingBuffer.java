package core.ui.buffers;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import program.main.Program;

public abstract class RenderingBuffer extends AbstractControl {
    private Node root;
    private Camera camera;
    private Texture2D texture;
    private boolean initialized;

    public RenderingBuffer(RenderManager renderManager, int width, int height){
        camera = new Camera(width, height);
        root = new Node("root");

        ViewPort viewPort = createViewPort(renderManager, camera);

        FrameBuffer offBuffer = new FrameBuffer(width, height, 1);
        texture = new Texture2D(width, height, Image.Format.RGBA8);
        texture.setMinFilter(Texture.MinFilter.Trilinear);
        texture.setMagFilter(Texture.MagFilter.Bilinear);

        offBuffer.setDepthBuffer(Image.Format.Depth);
        offBuffer.setColorTexture(texture);

        viewPort.setOutputFrameBuffer(offBuffer);

        setSpatial(root);
        viewPort.attachScene(root);

        Program.getInstance().getMainFrame().getGuiNode().addControl(this);
    }

    public abstract void setupCamera(Camera camera);
    public abstract void setupRoot(Node root);

    public ViewPort createViewPort(RenderManager renderManager, Camera camera){
        ViewPort viewPort = renderManager.createPreView("Rendering buffer", camera);
        viewPort.setClearFlags(true, true, true);

        return viewPort;
    }

    public Texture2D getTexture() {
        return texture;
    }

    @Override
    public final void setSpatial(Spatial spatial) {
        this.spatial = spatial;
        if (spatial != null)
            this.setEnabled(true);
        else
            this.setEnabled(false);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (enabled) {

            if (!initialized) {
                initialized = true;

                setupCamera(camera);
                setupRoot(root);
            }

            root.updateLogicalState(tpf);
            root.updateGeometricState();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
