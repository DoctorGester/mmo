import com.jme3.effect.ParticleEmitter
import com.jme3.effect.ParticleMesh
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Node;

ParticleEmitter fire = null;

def eventInit(Node attach) {
    fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30)
    Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
    matRed.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"))
    fire.setMaterial(matRed)
    fire.setImagesX(2); fire.setImagesY(2)
    fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f))
    fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f))
    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0))
    fire.setStartSize(1.5f)
    fire.setEndSize(0.1f)
    fire.setGravity(0, 0, 0)
    fire.setLowLife(0.5f)
    fire.setHighLife(3f)
    fire.getParticleInfluencer().setVelocityVariation(0.3f)
    attach.attachChild(fire)

    time = 15f;
}

def eventUpdate(float tpf){
    time -= tpf;
    if (time <= 0){
        fire.removeFromParent();
        return true
    }
    false
}