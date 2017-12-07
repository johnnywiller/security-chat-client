package br.furb.dss;

import java.security.KeyPair;

public class MessageEncryptor {
		
	
	public void encryptMessage(String msg) {
		
		DiffieHellmanUitls dh = new DiffieHellmanUitls();
		
		KeyPair keyPair = dh.generateKeyPair();
		
		dh.passPublicToServer(keyPair.getPublic(), out);
		
		
		
	}
	
}
