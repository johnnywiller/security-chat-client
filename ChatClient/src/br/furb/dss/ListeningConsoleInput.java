package br.furb.dss;

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

			Message msg = parseMessageString(read);

			if (msg == null)
				continue;

			try {

				encryptor.sendEncryptedMessage(msg);

			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Message parseMessageString(String message) {

		if (message == null || message.trim().isEmpty())
			return null;

		Message msg = new Message();

		String[] tokenized = message.split(" ");

		switch (tokenized[0]) {

		case "/msg":
			if (tokenized[1] == null || tokenized[1].isEmpty())
				return null;

			msg.setRecipient(tokenized[1]);

			msg.setMessage(message.substring(message.indexOf(tokenized[1]) + tokenized[1].length()));

			msg.setTimestamp(System.currentTimeMillis());

			return msg;

		case "/changeuser":
			break;

		}

		return null;
	}

}
