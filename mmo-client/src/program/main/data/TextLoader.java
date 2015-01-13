package program.main.data;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TextLoader implements AssetLoader {
	@Override
	public Object load(AssetInfo assetInfo) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(assetInfo.openStream()));
		List<String> result = new ArrayList<String>();
		for (;;) {
			String line = reader.readLine();
			if (line == null)
				break;

			result.add(line);
		}
		return new TextFile(result);
	}
}
