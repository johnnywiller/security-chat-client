package br.furb.dss;

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
		}
	}

	private void listen() throws Exception {

		synchronized (lock) {

			byte[] receivedPacket;

			while (true) {

				if (pause)
					lock.wait();

				receivedPacket = new byte[MAX_BUF];

				if (server.getIn().available() > 0) {
					server.getIn().read(receivedPacket);

					receivedPacket = getResizedPacket(receivedPacket);

					parsePacket(receivedPacket);

				} else {
					sleep(3000);
				}

			}

		}
	}

	private void parsePacket(byte[] packet) throws Exception {

		String smsg = new String(packet);

		String[] tokenized = smsg.split(" ");

		switch (tokenized[0]) {

		case "/msg":

			Message msg = decryptPacket(packet);

			System.out.println("received msg from [" + msg.getRecipient() + "] = " + msg.getMessage());

			break;

		case "/startsession":
			startSession(tokenized[1]);
			break;
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
