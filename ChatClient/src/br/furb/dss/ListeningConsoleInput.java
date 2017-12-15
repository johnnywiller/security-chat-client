package br.furb.dss;

import java.io.IOException;
import java.util.Scanner;

public class ListeningConsoleInput extends Thread {

	private ServerSocket server;
	private final int MAX_BUF = 255;
	private MessageEncryptor encryptor;

	public ListeningConsoleInput(ServerSocket server, MessageEncryptor encryptor) {
		this.server = server;
		this.encryptor = encryptor;
	}

	@Override
	public void run() {
		try {
			listen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listen() {

		Scanner sc = new Scanner(System.in);

		while (true) {

			String read = sc.nextLine();

			try {
				server.getOut().writeUTF(read);
				server.getOut().flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
