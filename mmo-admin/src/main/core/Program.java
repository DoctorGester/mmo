package main.core;

import core.main.LocalClient;
import main.net.API;
import main.ui.MainFrame;
import main.ui.ServerSelectionDialog;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author doc
 */
public class Program {
	private static Program instance;
	private MainFrame frame;
	private ServerSelectionDialog serverSelectionDialog;
	private LocalClient localClient;
	private API api;

	public static Program getInstance() {
		if (instance == null)
			instance = new Program();
		return instance;
	}

	private Program(){}

	private void start(){
		frame = new MainFrame();
		serverSelectionDialog = new ServerSelectionDialog(frame.getFrame());

		loadConfigurations();

		serverSelectionDialog.show();
	}

	private void loadConfigurations(){
		List<ServerConnectionConfiguration> configurationList = new ArrayList<ServerConnectionConfiguration>();

		configurationList.add(new ServerConnectionConfiguration("default", "127.0.0.1", 3637, "root", "root"));

		serverSelectionDialog.setConfigurations(configurationList);
	}

	public void connect(ServerConnectionConfiguration configuration){
		try {
			localClient = new LocalClient(0, new InetSocketAddress(configuration.getHost(), configuration.getPort()));

			api = new API(localClient, configuration.getUsername(), configuration.getPassword());
			api.checkCredentials();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void completeAuthorization(){
		serverSelectionDialog.hide();
	}

	public API getApi() {
		return api;
	}

	public MainFrame getFrame() {
		return frame;
	}

	public static void main(String ... args){
		getInstance().start();
	}
}
