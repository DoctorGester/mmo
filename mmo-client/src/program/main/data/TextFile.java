package program.main.data;

import java.util.ArrayList;
import java.util.List;

public class TextFile {
	private List<String> lines;

	public TextFile(List<String> lines){
		this.lines = new ArrayList<String>(lines);
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines.clear();
		this.lines.addAll(lines);
	}
}
