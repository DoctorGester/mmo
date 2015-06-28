package core.ui;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * Created by Gester on 6/25/2015.
 */
public class PortraitData {
    private Spatial scene;
    private int width, height;
    private Vector3f cameraLocation;
    private Vector3f cameraTarget;
    private ColorRGBA background;

    public Spatial getScene() {
        return scene;
    }

    public void setScene(Spatial scene) {
        this.scene = scene;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Vector3f getCameraLocation() {
        return cameraLocation;
    }

    public void setCameraLocation(Vector3f cameraLocation) {
        this.cameraLocation = cameraLocation;
    }

    public Vector3f getCameraTarget() {
        return cameraTarget;
    }

    public void setCameraTarget(Vector3f cameraTarget) {
        this.cameraTarget = cameraTarget;
    }

    public ColorRGBA getBackground() {
        return background;
    }

    public void setBackground(ColorRGBA background) {
        this.background = background;
    }
}
