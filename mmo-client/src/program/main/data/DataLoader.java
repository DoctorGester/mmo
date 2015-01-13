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
import core.main.DataUtil;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import program.main.Program;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author doc
 */
public class DataLoader {

    private Field findDataFieldWithName(Object from, String name){
        for (Field field: from.getClass().getDeclaredFields()){
            DataLoaderKey dataLoaderKey = field.getAnnotation(DataLoaderKey.class);

            if (dataLoaderKey != null){
                String value = dataLoaderKey.value();
                if (value.isEmpty())
                    value = field.getName();

                if (name.equalsIgnoreCase(value))
                    return field;
            }
        }

        return null;
    }

    public void loadFromStream(Object to, InputStream from) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(from));

        Pattern keyStringPattern = Pattern.compile("([a-zA-Z]*)\\s*:\\s*(.*)");

        for(String s = ""; s != null; s = reader.readLine()){
            Matcher keyStringMatcher = keyStringPattern.matcher(s);

            if (keyStringMatcher.matches()){
                String key = keyStringMatcher.group(1),
                       value = keyStringMatcher.group(2);

                Field toWrite = findDataFieldWithName(to, key);

                if (toWrite == null)
                    continue;

				DataLoaderKey dataLoaderKey = toWrite.getAnnotation(DataLoaderKey.class);

                Class<?> type = toWrite.getType();

				if (!dataLoaderKey.dataEnum().equals(Enum.class))
					type = dataLoaderKey.dataEnum();

				Object converted = value;

                if (type == int.class) {
                    converted = Integer.valueOf(value);
                } else if (type == boolean.class) {
					converted = Boolean.valueOf(value);
                } else if (type == double.class) {
					converted = Double.valueOf(value);
                } else if (type == float.class) {
					converted = Float.valueOf(value);
                } else if (Enum.class.isAssignableFrom(type)) {
					converted = Enum.valueOf(dataLoaderKey.dataEnum(), value);
                }

				if (dataLoaderKey.function().isEmpty()){
					if (!toWrite.isAccessible())
						toWrite.setAccessible(true);

					toWrite.set(to, converted);
				} else {
					Method method = to.getClass().getDeclaredMethod(dataLoaderKey.function(), converted.getClass());

					if (!method.isAccessible())
						method.setAccessible(true);

					method.invoke(to, converted);
				}
            }
        }

        from.close();
    }

	public GroovyScriptEngine loadScriptEngine(){
		try {
			String[] roots = new String[] {
					"res/units/scripts",
					"res/spells/unit/scripts",
					"res/spells/hero/scripts",
					"res/buffs/scripts",
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
			FileInputStream fis = new FileInputStream("map.map");
			byte data[] = new byte[fis.available()];
			fis.read(data);
			fis.close();
			Program.getInstance().setMapData(DataUtil.byteToBool(data));
			Program.getInstance().updateMap();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

	public <T, N> void loadDataList(String file, Class<T> idClass, Class<N> dataClass, Map<T, N> dataMap){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			for(String path = ""; path != null; path = reader.readLine()){
				File data = new File(path);
				if (!data.exists())
					continue;

				N instance = dataClass.newInstance();
				loadFromStream(instance, new FileInputStream(data));

				T id = idClass.cast(instance.getClass().getMethod("getId").invoke(instance));

				dataMap.put(id, instance);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Node loadAnimatedModelAlt(String file) throws IOException {
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
