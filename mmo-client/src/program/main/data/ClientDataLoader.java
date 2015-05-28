package program.main.data;

import com.jme3.animation.*;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.scene.plugins.ogre.AnimData;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import program.main.Program;
import shared.board.data.UnitData;
import shared.other.DataUtil;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author doc
 */
public class ClientDataLoader {
	private static Map<UnitData, Node> unitModels = new HashMap<UnitData, Node>();

	public GroovyScriptEngine loadScriptEngine(){
		try {
			String[] roots = new String[] {
					"res/data",
					"res/ui/scripts",
					"res/effects"
			};
			return new GroovyScriptEngine(roots);
		} catch (IOException e) {
			e.printStackTrace();
		}

        return null;
	}

	public void loadMap(){
		try {
			FileInputStream fis = new FileInputStream("res/map.map");
			byte data[] = new byte[fis.available()];
			fis.read(data);
			fis.close();
			Program.getInstance().setMapData(DataUtil.byteToBool(data));
			//Program.getInstance().updateMap();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Node getUnitModel(UnitData data){
		try {

			Node model = unitModels.get(data);

			if (model == null) {
				model = loadAnimatedModelAlt(data.getModelPath());
				model.setLocalScale((float) data.getScale());
				model.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
				unitModels.put(data, model);
			}

			return model.clone(false);
		} catch (IOException e) {
			e.printStackTrace();
			return new Node();
		}
	}

	public void loadSpecialEffectsFromFileSystem(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("res/effects/datalist")));
			for(String path = ""; path != null; path = reader.readLine()){
				String params[] = path.split("\\s", 2);

				if (params.length == 2)
					Program.getInstance().getEffectScriptMap().put(params[0], params[1]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Node loadAnimatedModelAlt(String file) throws IOException {
		String folder = new File(file).getParent();
		String name = new File(file).getName();

		AssetManager assetManager = Program.getInstance().getMainFrame().getAssetManager();

		String path = FilenameUtils.separatorsToUnix(new File(folder, name + ".mesh.xml").getPath()); // It's important to have unix separators
		ModelKey key = new ModelKey(path);
		boolean fromCache = assetManager instanceof DesktopAssetManager && ((DesktopAssetManager) assetManager).getFromCache(key) != null;

		Node node = (Node) assetManager.loadModel(key);
		node.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

		File list[] = new File(folder).listFiles();

		if (list != null)
			for (File animationFile: list) {
				String fileName = animationFile.getName();

				if (fileName.startsWith(name) && fileName.endsWith("skeleton.xml")) {
					path = FilenameUtils.separatorsToUnix(new File(folder, fileName).getPath());
					AnimData data;

					// That's a hack to fix some broken files
					try {
						data = (AnimData) assetManager.loadAsset(path);
					} catch (AssetLoadException e){
						FileInputStream input = new FileInputStream(path);

						String content = IOUtils.toString(input);
						content = content.replaceAll("(#IND|#INF)", "0");

						FileOutputStream output = new FileOutputStream(path);
						IOUtils.write(content, output);

						input.close();
						output.close();

						data = (AnimData) assetManager.loadAsset(path);
					}

					AnimControl control = node.getControl(AnimControl.class);

					if (control == null){
						if (!fromCache)
							node.depthFirstTraversal(new SceneGraphVisitor() {
								@Override
								public void visit(Spatial spatial) {
									if (spatial instanceof Geometry) {
										Mesh m = ((Geometry) spatial).getMesh();

										m.generateBindPose(true);
									}
								}
							});

						control = new AnimControl(data.skeleton);
						node.addControl(control);

						SkeletonControl skeletonControl = new SkeletonControl(data.skeleton);
						node.addControl(skeletonControl);
					}

					Animation old = data.anims.get(0);

					Animation animation = new Animation(fileName.substring(name.length() + 1, fileName.indexOf(".")), old.getLength());
					animation.setTracks(old.getTracks());

					control.addAnim(animation);
				}
			}

		return node;
	}

	public Spatial loadAnimatedModel(String path, String animationFile){
		AssetManager assetManager = Program.getInstance().getMainFrame().getAssetManager();
		Spatial spatial = assetManager.loadModel(path);
		spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

		Object asset = assetManager.loadAsset(animationFile);

		AnimControl animControl = spatial.getControl(AnimControl.class);

		if (asset instanceof TextFile){
			List<String> animations = ((TextFile) asset).getLines();

			for (String animation: animations){
				StringTokenizer tokenizer = new StringTokenizer(animation);

				String name = tokenizer.nextToken();
				int startFrame = Integer.valueOf(tokenizer.nextToken());
				int length = Integer.valueOf(tokenizer.nextToken());

				splitAnimation(animControl, name, startFrame, startFrame + length);
			}
		}

		return spatial;
	}

	private static void splitAnimation(AnimControl control, String name, int startFrame, int endFrame) {
		Animation animation = control.getAnim("default_skl");

		Animation newAnim;
		float newLength = Float.MIN_VALUE;
		Track[] tracks = new Track[animation.getTracks().length];

		float relation = animation.getLength() / (float) ((BoneTrack) animation.getTracks()[0]).getTimes().length;
		float startTime = relation * startFrame;
		float endTime = relation * endFrame;

		for (int j = 0; j < animation.getTracks().length; j++) {
			BoneTrack track = (BoneTrack) animation.getTracks()[j];

			tracks[j] = splitBoneTrack(track, startTime, endTime);

			float length = tracks[j].getLength();

			if (Math.abs(length - endTime + startTime) < 0.05)
				newLength = Math.max(length, newLength);
		}

		newAnim = new Animation(name, newLength);
		newAnim.setTracks(tracks);
		control.addAnim(newAnim);
	}

	private static BoneTrack splitBoneTrack(BoneTrack track, float startTime, float endTime) {
		Quaternion[] oldRots = track.getRotations();
		Vector3f[] oldTrans = track.getTranslations();
		Vector3f[] oldScales = track.getScales();
		float[] oldTimes = track.getTimes();

		int start = 0;

		for (int i = oldTimes.length - 1; i >= 0; i--)
			if (oldTimes[i] <= startTime){
				start = i;
				break;
			}

		int end = oldTimes.length - 1;

		for (int i = 0; i < oldTimes.length; i++)
			if (oldTimes[i] >= endTime) {
				end = i;
				break;
			}

		float[] newTimes = new float[end - start];
		Vector3f[] newTranslations = new Vector3f[end - start];
		Vector3f[] newScales = new Vector3f[end - start];
		Quaternion[] newRotations = new Quaternion[end - start];

		for (int i = start; i < end; i++) {
			int newFrame = i - start;

			newTimes[newFrame] = oldTimes[i] - oldTimes[start];
			newTranslations[newFrame] = oldTrans[i].clone();
			newScales[newFrame] = oldScales[i].clone();
			newRotations[newFrame] = oldRots[i].clone();
		}

		return new BoneTrack(track.getTargetBoneIndex(), newTimes, newTranslations, newRotations, newScales);
	}
}
