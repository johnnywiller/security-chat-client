package br.furb.dss;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryptor {

	private ServerSocket server;
	private Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

	private Cipher serverEncryptor = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	private Cipher serverDecryptor = Cipher.getInstance("AES/CBC/PKCS5PADDING");

	private ListeningServer listenServer;

	private byte[] ourUserName;
	private final int MAX_NAME_SIZE = 10;

	public MessageEncryptor(ServerSocket server, String ourUserName) throws Exception {
		this.server = server;
		this.ourUserName = Arrays.copyOf(ourUserName.getBytes(), MAX_NAME_SIZE);
	}

	private void initializeServerEncryptors() {

		// serverEncryptor.ini

	}

	public void setListenServer(ListeningServer listenServer) {
		this.listenServer = listenServer;
	}

	public void sendEncryptedMessage(Message message) throws Exception {

		// get user keys
		DestinationUser destUser = ClientsKeyStore.getInstance().getUser(message.getRecipient());

		// we don't have made a handshake with this client yet
		// so we need to establish a session
		if (destUser == null) {
			System.out.println("Initiating session");
			listenServer.pauseListen();

			destUser = ClientSessionInitiation.getInstance(server).startSession(message.getRecipient(), true);

			listenServer.resumeListen();

		}
		System.out.println("Session Initiated");
		// sets cipher to destination user
		configureCipher(destUser, true);

		byte[] iv = cipher.getIV();

		cipher.update(longToBytes(message.getTimestamp()));
		byte[] cipherText = cipher.doFinal(message.getMessage().getBytes());

		// copy IV and cipher to a new array to send over the network
		byte[] packet = new byte[cipherText.length + iv.length + MAX_NAME_SIZE + 1];

		byte[] bDestUser = String.format("%1$10s", destUser.getName()).getBytes();

		System.arraycopy(bDestUser, 0, packet, 1, MAX_NAME_SIZE);
		System.arraycopy(iv, 0, packet, MAX_NAME_SIZE + 1, iv.length);
		System.arraycopy(cipherText, 0, packet, MAX_NAME_SIZE + iv.length + 1, cipherText.length);

		// set the packet size
		packet[0] = ((byte) (packet.length - 1));

		// send packet to server
		server.getOut().write(packet);
		server.getOut().flush();

		System.out.println("Message sent");
	}

	public Message decryptMessage(byte[] packet) throws Exception {

		Message message = new Message();
		System.out.println("msg received");
		// extract the user from the packet
		byte[] bytesFromUser = Arrays.copyOf(packet, 10);

		String fromUser = new String(bytesFromUser).trim();

		// get user keys
		DestinationUser from = ClientsKeyStore.getInstance().getUser(fromUser);

		// we don't have made a handshake with this client yet
		// so we need to establish a session
		if (from == null) {
			throw new Exception("Client isn't in the keystore");
		}

		byte[] iv = Arrays.copyOfRange(packet, 10, 26);
		from.setIv(iv);

		configureCipher(from, false);

		byte[] cipherText = Arrays.copyOfRange(packet, 26, packet.length);

		byte[] plainText = cipher.doFinal(cipherText);

		long timestamp = bytesToLong(Arrays.copyOf(plainText, Long.BYTES));

		byte[] msg = Arrays.copyOfRange(plainText, Long.BYTES + 1, plainText.length);

		message.setMessage(new String(msg));
		message.setTimestamp(timestamp);
		message.setRecipient(fromUser);

		return message;
	}

	private Cipher configureCipher(DestinationUser dest, boolean encrypt)
			throws InvalidKeyException, InvalidAlgorithmParameterException {

		byte[] iv = new byte[16];
		if (encrypt) {
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
		} else {
			iv = dest.getIv();
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(dest.getSymmetricKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

		return cipher;
	}

	public void encryptToServer(String message) {

	}

	private byte[] requestUserPublicKey(String destUser) throws IOException {

		server.getOut().writeUTF("get public key :" + destUser);

		return null;
	}

	public static byte[] longToBytes(long l) {
		byte[] result = new byte[Long.SIZE / Byte.SIZE];
		for (int i = 7; i >= 0; i--) {
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}

	public static long bytesToLong(byte[] b) {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}

}
