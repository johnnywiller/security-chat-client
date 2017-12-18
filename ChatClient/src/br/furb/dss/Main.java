package br.furb.dss;

import java.util.Random;

public class Main {

	public static void main(String[] args) throws Exception {
		
		String s = (new Random().nextInt()) % 1000 + "";
		
		ServerSocket server = new ServerSocket();
	
		MessageEncryptor msgEncryptor = new MessageEncryptor(server, s);
		
		ListeningServer listening = new ListeningServer(server, msgEncryptor);
	
		msgEncryptor.setListenServer(listening);
		
		ListeningConsoleInput listeningInput = new ListeningConsoleInput(server, msgEncryptor);

		listening.start();
		listeningInput.start();
		

	}

}
