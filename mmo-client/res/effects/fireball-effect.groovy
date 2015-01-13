import com.jme3.effect.influencers.*
import com.jme3.effect.shapes.*
import com.jme3.effect.*;
import com.jme3.material.Material
import com.jme3.math.*;
import com.jme3.scene.*;

def init(){
	Node test = new Node()
	test.move(80, 0, 0)
	eventInit(root, test)
}

def eventInit(Node from, Node target) {
	emitter = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 200)
	Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
	matRed.setTexture("Texture", assetManager.loadTexture("res/textures/particles/smoke01.png"))
	def influencer = new NewtonianParticleInfluencer()
	
	emitter.setParticleInfluencer(new RadialParticleInfluencer())
	emitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 1.0f))
	emitter.setMaterial(matRed)
	emitter.setEndColor(new ColorRGBA(0.9f, 0.7f, 0f, 1f))
	emitter.setStartColor(new ColorRGBA(0.8f, 0.2f, 0f, 0.5f))
	emitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0))
	emitter.setStartSize(1.3f)
	emitter.setEndSize(0.1f)
	emitter.setGravity(0, 0, 0)
	emitter.setLowLife(0.5f)
	emitter.setHighLife(1.5f)
	emitter.getParticleInfluencer().setVelocityVariation(0.4f)
	emitter.setParticlesPerSec(150)

	attach = new Node()
	attach.setLocalTranslation(from.getLocalTranslation())
	attach.move(0, 3f, 0)
	attach.attachChild(emitter)

	to = target

	root.attachChild(attach)
}

def eventUpdate(float tpf){
	final float SPEED = 30f;
	
	def position = attach.getLocalTranslation(),
	    target = to.getLocalTranslation().add(0, 3, 0),
	    norm = target.subtract(position)
	    
	if (norm.length() < SPEED * tpf){
		emitter.setParticlesPerSec(0);
		if (emitter.getNumVisibleParticles() == 0){
			emitter.removeFromParent()
			return true
		}
	} else {
		attach.move(norm.normalize().mult((tpf * SPEED) as float))
	}
	false
}