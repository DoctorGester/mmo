package main.core;

/**
 * @author doc
 */
public class ServerConnectionConfiguration {

	private String name;
	private String host;
	private int port;
	private String username, password;

	public ServerConnectionConfiguration(String name, String host, int port, String username, String password) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return name;
	}
}
