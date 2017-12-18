package br.furb.dss;

import java.awt.SecondaryLoop;
import java.io.IOException;
import java.util.Random;

public class Main {

	public static void main(String[] args) throws Exception {
		
		byte[] pub = Signer.getInstance().getPublicKey();
		
		String s = (new Random().nextInt()) % 1000 + "";

		ServerSocket server = new ServerSocket();

		MessageEncryptor msgEncryptor = new MessageEncryptor(server, s);

		ChatActions action = new ChatActions(msgEncryptor);

		sendPublicKeyServer(server);

		action.sendAction(EChatActions.CHANGE_USERNAME, s);

		ListeningServer listening = new ListeningServer(server, msgEncryptor);

		msgEncryptor.setListenServer(listening);

		ListeningConsoleInput listeningInput = new ListeningConsoleInput(server, msgEncryptor);

		listening.start();
		listeningInput.start();

	}

	private static void sendPublicKeyServer(ServerSocket server) throws IOException {

		byte[] pubKey = Signer.getInstance().getPublicKey();

		server.getOut().write(pubKey);

	}

}
