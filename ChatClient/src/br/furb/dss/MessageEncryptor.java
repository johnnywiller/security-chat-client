package br.furb.dss;

import java.io.IOException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryptor {

	private ServerSocket server;
	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

	public MessageEncryptor(ServerSocket server) throws Exception {
		this.server = server;
	}

	public void sendEncryptedMessage(String msg, String destUser) throws Exception {

		// get user keys
		DestinationUser dest = ClientsKeyStore.getInstance().getUser(destUser);

		// we don't have made a handshake with this client yet
		// so we need to establish a session
		if (dest == null) {
			dest = ClientSessionInitiation.getInstance().startSession(destUser);
		}

		byte[] iv = dest.getIv();
		SecretKeySpec secretKeySpec = new SecretKeySpec(dest.getSymmetricKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

		byte[] cipherText = cipher.doFinal(msg.getBytes());
		
		dest.setIv(cipher.getIV());
		
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
	}

	

	private byte[] requestUserPublicKey(String destUser) throws IOException {

		server.getOut().writeUTF("get public key :" + destUser);

		return null;
	}

}
