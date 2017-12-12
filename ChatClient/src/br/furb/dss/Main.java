package br.furb.dss;

import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//MessageEncryptor encryptor = new MessageEncryptor();
		
		//encryptor.encryptMessage("bom dia blumenau", "pottmayer");
		
		ServerSocket server = new ServerSocket();
		
		Scanner sc = new Scanner(System.in);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true) {
					
					String read = sc.nextLine();
					
					try {
						server.getOut().writeUTF(read);
						server.getOut().flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}

			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true) {
					
					try {
						System.out.println("Received: " + server.getIn().readUTF());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}

			}
		}).start();

	}

}
