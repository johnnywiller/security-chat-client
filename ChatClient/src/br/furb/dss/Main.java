package br.furb.dss;

import java.util.Random;

public class Main {

	public static void main(String[] args) throws Exception {
		String s = (new Random().nextInt()) % 1000 + "";
		ServerSocket server = new ServerSocket();
		
		MessageEncryptor msgEncryptor = new MessageEncryptor(server, s);
		ListeningServer listening = new ListeningServer(server, msgEncryptor);
		ListeningConsoleInput listeningInput = new ListeningConsoleInput(server, msgEncryptor);
		
		listening.start();
		listeningInput.start();
		
//		Scanner sc = new Scanner(System.in);
//		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				
//				while(true) {
//					
//					String read = sc.nextLine();
//					
//					try {
//						server.getOut().writeUTF(read);
//						server.getOut().flush();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//				}
//
//			}
//		}).start();
//		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				
//				while(true) {
//					
//					try {
//						System.out.println("Received: " + server.getIn().readUTF());
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//				}
//
//			}
//		}).start();

	}

}
