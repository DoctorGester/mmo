package main.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author doc
 */
public class MainFrame {
	private JFrame frame;
	private JTabbedPane tabbedPane;
	private PlayerForm playerForm;

	public MainFrame(){
		setLookAndFeel();

		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setTitle("mmo-admin");
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.setLocationRelativeTo(null);

		playerForm = new PlayerForm();

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Player", playerForm.getBasePanel());

		frame.getContentPane().add(tabbedPane);

		frame.setVisible(true);
	}

	private static void setLookAndFeel(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public PlayerForm getPlayerForm() {
		return playerForm;
	}
}
