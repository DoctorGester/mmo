package main.ui;

import main.core.ServerConnectionConfiguration;

import javax.swing.*;

/**
 * @author doc
 */
public class ServerSelectionForm {
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JComboBox configurationComboBox;
	private JButton loginButton;
	private JPanel basePanel;
	private JTextField ipField;
	private JTextField portField;

	public JButton getLoginButton() {
		return loginButton;
	}

	public JPanel getBasePanel() {
		return basePanel;
	}

	public JTextField getUsernameField() {
		return usernameField;
	}

	public JTextField getIpField() {
		return ipField;
	}

	public JTextField getPortField() {
		return portField;
	}

	public JPasswordField getPasswordField() {
		return passwordField;
	}

	public JComboBox getConfigurationComboBox() {
		return configurationComboBox;
	}
}
