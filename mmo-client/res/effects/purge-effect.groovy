import com.jme3.effect.influencers.*
import com.jme3.effect.shapes.*
import com.jme3.effect.ParticleEmitter
import com.jme3.effect.ParticleMesh
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Node;

ParticleEmitter emitter = null;

def eventInit(Node attach) {
	emitter = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 300)
	Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
	matRed.setTexture("Texture", assetManager.loadTexture("res/textures/particles/ember01.png"))
	def influencer = new NewtonianParticleInfluencer()
	
	emitter.setParticleInfluencer(new RadialParticleInfluencer())
	emitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 3.0f));
	emitter.setMaterial(matRed)
	emitter.setEndColor(new ColorRGBA(0.4f, 0.6f, 1f, 1f))
	emitter.setStartColor(new ColorRGBA(0.8f, 0.4f, 1f, 0.5f))
	emitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 5, 0))
	emitter.setStartSize(0.8f)
	emitter.setEndSize(0.1f)
	emitter.setGravity(0, 0, 0)
	emitter.setLowLife(0.5f)
	emitter.setHighLife(1f)
	emitter.getParticleInfluencer().setVelocityVariation(0.6f)

	attach.attachChild(emitter)
    emitter.emitAllParticles()

	time = 0.0f;
}

def eventUpdate(float tpf){
	time -= tpf;
	if (time <= 0){
		emitter.setParticlesPerSec(0);
		if (emitter.getNumVisibleParticles() == 0){
			emitter.removeFromParent();
			return true
		}
	}
	false
}