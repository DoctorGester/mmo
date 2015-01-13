package core.handlers;

import core.exceptions.IncorrectHeaderException;
import core.main.*;
import program.main.Program;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Deprecated
public class QueryMessageHandler extends PacketHandler{

	private Program program;

	public QueryMessageHandler(byte header[]){
		super(header);
		this.program = Program.getInstance();
	}

	public void handle(LocalServer localServer, Client client, Packet data) {
		try {
			String sql = new String(data.getData(), "UTF-8");
			PreparedStatement st = null;//program.getDatabase().getConnection().prepareStatement(sql);
			ResultSet resultSet = st.executeQuery();
			if (!resultSet.next()){
				Packet p = new Packet();
				p.setData(data.getHeader(), "Success".getBytes("UTF-8"));
				localServer.send(client, p);
			} else {
				String s = "| ";
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int cc = rsmd.getColumnCount();
				for(int i = 0; i < cc; i++)
					s += rsmd.getColumnLabel(i + 1) + " | ";
				int l = s.length() - 1;
				s += "\n";
				for(int i = 0; i < l; i++)
					s += "-";
				s += "\n| ";
				
				while(resultSet.next()){
					Object[] obs = program.getDatabase().fetch(resultSet);
					for(Object o: obs)
						if (o != null)
							s += o.toString() + " | ";
						else
							s += "null | ";
					s += "\n| ";
				}
				if (s.endsWith("| "))
					s = s.substring(0, s.length() - 2);
				if (!s.endsWith("\n"))
					s += "\n";
				for(int i = 0; i < l; i++)
					s += "-";
				
				Packet p = new Packet();
				p.setData(data.getHeader(), s.getBytes("UTF-8"));
				localServer.send(client, p);
		
			}
		} catch (SQLException e) {
			try {
				Packet p = new Packet();
				p.setData(data.getHeader(), e.getMessage().getBytes("UTF-8"));
				localServer.send(client, p);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IncorrectHeaderException e1) {
				e1.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IncorrectHeaderException e) {
			e.printStackTrace();
		}
	}

	public void handle(LocalClient localClient, Packet data) {
	}


}
