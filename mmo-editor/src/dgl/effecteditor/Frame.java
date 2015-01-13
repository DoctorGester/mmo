package dgl.effecteditor;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.concurrent.Callable;

/**
 * @author doc
 */
public class Frame extends JFrame {
	private static final Dimension SIZE = new Dimension(1200, 700);

	private JPanel panel;
	private RSyntaxTextArea textArea;
	private RTextScrollPane scrollPane;
	private SwingCanvas canvas;

	public Frame(){
		super();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		int w = SIZE.width,
			h = SIZE.height,
			x = (dim.width - w)/2,
			y = (dim.height - h)/2;

		setSize(SIZE);
		setLocation(x, y);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		createComponents();

		setVisible(true);
	}

	private void saveCode(){
		try {
			PrintWriter writer = new PrintWriter("last-effect.groovy", "UTF-8");
			writer.print(textArea.getText());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void tryToLoadCode(){
		try {
			StringBuilder fileData = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader("last-effect.groovy"));
			char[] buf = new char[1024];
			int numRead;
			while((numRead=reader.read(buf)) != -1){
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
			reader.close();
			textArea.setText(fileData.toString());
			tryToExecuteScript();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void tryToExecuteScript(){
		canvas.enqueue(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					GroovyClassLoader gcl = new GroovyClassLoader();
					Class groovyClass = gcl.parseClass(textArea.getText(), "Effect.groovy");
					GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
					groovyObject.setProperty("root", canvas.getRootNode());
					groovyObject.setProperty("assetManager", canvas.getAssetManager());
					groovyObject.setProperty("canvas", canvas);
					groovyObject.invokeMethod("init", new Object[0]);
					canvas.setScript(groovyObject);
				} catch (GroovyRuntimeException e){
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	private void createComponents(){
		Theme theme = null;
		try {
			theme = Theme.load(Frame.class.getClassLoader().getResourceAsStream("dgl/dark.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		setLayout(new GridLayout(1, 1));

		panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));

		canvas = new SwingCanvas();

		add(canvas.getPanel());
		add(panel);

		textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		textArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S){
					canvas.clear();
					tryToExecuteScript();
					saveCode();
				}
			}
		});

		if (theme != null)
			theme.apply(textArea);

		scrollPane = new RTextScrollPane(textArea);
		scrollPane.setFoldIndicatorEnabled(true);

		panel.add(scrollPane);

		tryToLoadCode();
	}
}
