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
	emitter = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 200)
	Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
	matRed.setTexture("Texture", assetManager.loadTexture("res/textures/particles/ember01.png"))
	def influencer = new NewtonianParticleInfluencer()
	
	emitter.setParticleInfluencer(new RadialParticleInfluencer())
	emitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
	emitter.setMaterial(matRed)
	emitter.setEndColor(new ColorRGBA(0.4f, 1f, 0f, 1f))
	emitter.setStartColor(new ColorRGBA(0.8f, 0.8f, 0f, 0.5f))
	emitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 9, 0))
	emitter.setStartSize(1.0f)
	emitter.setEndSize(0.1f)
	emitter.setGravity(0, 0, 0)
	emitter.setLowLife(0.5f)
	emitter.setHighLife(3f)
	emitter.getParticleInfluencer().setVelocityVariation(0.1f)
	emitter.setParticlesPerSec(80)
	attach.attachChild(emitter)
	
	time = 2f;
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