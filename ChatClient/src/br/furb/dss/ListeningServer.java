package br.furb.dss;

import java.io.IOException;
import java.util.Arrays;

public class ListeningServer extends Thread {

	private ServerSocket server;
	private final int MAX_BUF = 255;
	private MessageEncryptor encryptor;

	private Object lock = new Object();

	private volatile boolean pause;

	public ListeningServer(ServerSocket server, MessageEncryptor encryptor) throws Exception {
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

	public void pauseListen() throws InterruptedException {
		pause = true;
	}

	public void resumeListen() {
		synchronized (lock) {
			lock.notify();
			pause = false;
		}
	}

	private void listen() throws Exception {

		synchronized (lock) {

			byte[] receivedPacket;

			while (true) {

				if (pause)
					lock.wait();

				if (server.getIn().available() > 0) {
					
					System.out.println("read");
					
					receivedPacket = new byte[MAX_BUF];

					server.getIn().read(receivedPacket);

					receivedPacket = getResizedPacket(receivedPacket);

					parsePacket(receivedPacket);

				}
			}

		}
	}

	private void parsePacket(byte[] packet) throws Exception {

		String smsg = new String(packet);

		String[] tokenized = smsg.split(" ");

		switch (tokenized[0]) {

		case "/startsession":
			System.out.println("start session");
			startSession(tokenized[1]);
			break;
		case "/online":
			printOnline();
			break;
		default:
			Message msg = decryptPacket(packet);

			System.out.println("received msg from [" + msg.getRecipient() + "] = " + msg.getMessage());

			break;

		}

	}

	private void printOnline() throws IOException {

		String online = server.getIn().readLine();

		while (online != "/endonline") {
			System.out.println("[" + online + "]");
		}

	}

	private void startSession(String client) throws Exception {
		ClientSessionInitiation.getInstance(server).startSession(client, false);
	}

	private Message decryptPacket(byte[] packet) throws Exception {

		Message msg = encryptor.decryptMessage(packet);

		return msg;
	}

	private byte[] getResizedPacket(byte[] packet) {
		byte[] resized;
		byte size = packet[0];

		resized = Arrays.copyOfRange(packet, 1, size + 1);

		return resized;
	}

}
