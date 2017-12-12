package br.furb.dss;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//MessageEncryptor encryptor = new MessageEncryptor();
		
		//encryptor.encryptMessage("bom dia blumenau", "pottmayer");
		
		ServerSocket server = new ServerSocket();
		
		Scanner sc = new Scanner(System.in);
		while(true) {
			
			String read = sc.next();
			
			server.getOut().writeUTF(read);
			server.getOut().flush();
		}

	}

}
