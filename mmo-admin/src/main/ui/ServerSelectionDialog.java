package main.ui;

import main.core.Program;
import main.core.ServerConnectionConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * @author doc
 */
public class ServerSelectionDialog {
	private final JDialog dialog;
	private final ServerSelectionForm serverSelectionForm;

	public ServerSelectionDialog(JFrame main){
		serverSelectionForm = new ServerSelectionForm();

		dialog = new JDialog(main, Dialog.ModalityType.APPLICATION_MODAL);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		dialog.getContentPane().add(serverSelectionForm.getBasePanel());
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);

		serverSelectionForm.getConfigurationComboBox().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onConfigurationUpdate();
			}
		});

		serverSelectionForm.getLoginButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Program.getInstance().connect((ServerConnectionConfiguration) serverSelectionForm.getConfigurationComboBox().getSelectedItem());
			}
		});
	}

	public void show(){
		dialog.setVisible(true);
	}

	public void hide(){
		dialog.setVisible(false);
	}

	public void onConfigurationUpdate(){
		ServerConnectionConfiguration configuration = (ServerConnectionConfiguration) serverSelectionForm.getConfigurationComboBox().getSelectedItem();

		serverSelectionForm.getIpField().setText(configuration.getHost());
		serverSelectionForm.getPortField().setText(String.valueOf(configuration.getPort()));
		serverSelectionForm.getUsernameField().setText(configuration.getUsername());
		serverSelectionForm.getPasswordField().setText(configuration.getPassword());
	}

	public void setConfigurations(List<ServerConnectionConfiguration> configurationList) {
		ServerConnectionConfiguration[] configurations = configurationList.toArray(new ServerConnectionConfiguration[configurationList.size()]);
		serverSelectionForm.getConfigurationComboBox().setModel(new DefaultComboBoxModel<ServerConnectionConfiguration>(configurations));

		onConfigurationUpdate();
	}
}
