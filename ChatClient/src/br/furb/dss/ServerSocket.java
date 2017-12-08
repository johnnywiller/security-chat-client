package br.furb.dss;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerSocket {

	private ObjectOutputStream out;
	private ObjectInputStream in;

	public ServerSocket() {
		String serverName = "54.149.156.8";
		int port = 6678;

		try {
			Socket client = new Socket(serverName, port);

			OutputStream outToServer = client.getOutputStream();
			this.out = new ObjectOutputStream(outToServer);

			InputStream inFromServer = client.getInputStream();
			this.in = new ObjectInputStream(inFromServer);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ObjectOutputStream getOut() {
		return out;
	}

	public ObjectInputStream getIn() {
		return in;
	}

}
