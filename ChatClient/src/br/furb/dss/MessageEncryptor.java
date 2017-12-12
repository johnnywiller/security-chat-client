package br.furb.dss;

import java.io.IOException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryptor {

	private ServerSocket server;
	private HashMap<String, DestinationUser> destinationUsers;	
	private byte[] symmetricKey;
	private byte[] macKey;
	
	public MessageEncryptor() throws Exception {
		this.server = new ServerSocket();
		DiffieHellmanUitls dh = new DiffieHellmanUitls();
		DHPublicKey publicKey;
		KeyPair keyPair;
		byte[] secret;
		destinationUsers = new HashMap<>();
		
		// --------- DH Kex PROCESS --------- 
		
		// compute DH keys ('a' and A = g^a mod p)
		keyPair = dh.generateKeyPair();

		// pass A (A = g^a mod p) to the server
		dh.passPublicToServer((DHPublicKey) keyPair.getPublic(), server.getOut());
		
		// get B (g^b mod p) from the server
		publicKey = dh.getServerPublic(server.getIn());
		
		// compute secret (s = B^a mod p)
		secret = dh.computeDHSecretKey((DHPrivateKey) keyPair.getPrivate(), publicKey);

		// use SHA2 to derive key
		secret = MessageDigest.getInstance("SHA-256").digest(secret);

		// 128 bits for symmetric key encryption and 128 bits for message authentication
		this.symmetricKey = Arrays.copyOf(secret, 16);
		this.macKey = Arrays.copyOfRange(secret, 16, 32);

	}

	public void encryptMessage(String msg, String destUser) throws Exception {
		
		// adds new user to our key store
		if (!destinationUsers.containsKey(destUser)) {
			addDestinationUser(destUser);
		}
		
		SecretKeySpec secretKeySpec = new SecretKeySpec(symmetricKey, "AES");

		byte[] iv = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

		byte[] cipherText = cipher.doFinal(msg.getBytes());

		// copy IV and cipher to a new array to send over the network
		byte[] packet = new byte[cipherText.length + iv.length + 1];
		System.arraycopy(iv, 0, packet, 1, iv.length);
		System.arraycopy(cipherText, 0, packet, iv.length + 1, cipherText.length);

		System.out.println(Arrays.toString(cipherText));

		// set the packet size
		packet[0] = ((byte) (packet.length - 1));

		// send packet to server
		server.getOut().write(packet);
		server.getOut().flush();

		System.out.println("Message sent");
		// System.out.println("CLIENT secret");
		// System.out.println(Arrays.toString(secret));
	}
	
	private void addDestinationUser(String destUser) {
		
		DestinationUser user = new DestinationUser();
		
		
	}
	
	private byte[] requestUserPublicKey(String destUser) throws IOException {
		
		server.getOut().writeUTF("get public key :" + destUser);
		
		
		return null;
	}
	
}
