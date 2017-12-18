package br.furb.dss;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws Exception {
		
		byte[] pub = Signer.getInstance().getPublicKey();
		
		ServerSocket server = new ServerSocket();

		MessageEncryptor msgEncryptor = new MessageEncryptor(server);

		//ChatActions action = new ChatActions(msgEncryptor);

		sendPublicKeyServer(server);

		//action.sendAction(EChatActions.CHANGE_USERNAME);

		ListeningServer listening = new ListeningServer(server, msgEncryptor);

		msgEncryptor.setListenServer(listening);

		ListeningConsoleInput listeningInput = new ListeningConsoleInput(server, msgEncryptor);

		listening.start();
		listeningInput.start();

	}

	private static void sendPublicKeyServer(ServerSocket server) throws IOException {

		byte[] pubKey = Signer.getInstance().getPublicKey();

		server.getOut().write(pubKey);
		server.getOut().flush();

	}

}
