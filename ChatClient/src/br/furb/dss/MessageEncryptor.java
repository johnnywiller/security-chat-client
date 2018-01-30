package br.furb.dss;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessageEncryptor {

	private ServerSocket server;
	private Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

	private final int HMAC_SIZE = 32;

	Mac hasher = Mac.getInstance("HmacSHA256");

	private ListeningServer listenServer;

	private final int MAX_NAME_SIZE = 10;

	public MessageEncryptor(ServerSocket server) throws Exception {
		this.server = server;
		
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
			
			//Thread.sleep(1000);
			
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
		byte[] packet = new byte[HMAC_SIZE + cipherText.length + iv.length + MAX_NAME_SIZE + 1];

		byte[] bDestUser = String.format("%1$10s", destUser.getName()).getBytes();

		byte[] hash = generateHMAC(iv, cipherText, destUser.getMacKey());

		System.arraycopy(bDestUser, 0, packet, 1, MAX_NAME_SIZE);
		System.arraycopy(hash, 0, packet, MAX_NAME_SIZE + 1, hash.length);
		System.arraycopy(iv, 0, packet, MAX_NAME_SIZE + 1 + hash.length, iv.length);
		System.arraycopy(cipherText, 0, packet, MAX_NAME_SIZE + hash.length + iv.length + 1, cipherText.length);

		// set the packet size
		packet[0] = ((byte) (packet.length - 1));

		// send packet to server
		server.getOut().write(packet);
		server.getOut().flush();

		System.out.println("Message sent");
	}

	private byte[] generateHMAC(byte[] iv, byte[] cipherText, byte[] macKey) throws InvalidKeyException {

		byte[] packet = new byte[iv.length + cipherText.length];
		System.arraycopy(iv, 0, packet, 0, iv.length);
		System.arraycopy(cipherText, 0, packet, iv.length, cipherText.length);

		hasher.init(new SecretKeySpec(macKey, "HmacSHA256"));

		byte[] hash = hasher.doFinal(packet);

		return hash;
	}

	public Message decryptMessage(byte[] packet) throws Exception {

		Message message = new Message();
		System.out.println("msg received");
		// extract the user from the packet

		byte[] bytesFromUser = Arrays.copyOf(packet, MAX_NAME_SIZE);

		String fromUser = new String(bytesFromUser).trim();

		// get user keys
		DestinationUser from = ClientsKeyStore.getInstance().getUser(fromUser);

		// we don't have made a handshake with this client yet
		// so we need to establish a session
		if (from == null) {
			throw new Exception("Client isn't in the keystore");
		}

		// HMAC-SHA256 size is 32
		byte[] hash = Arrays.copyOfRange(packet, MAX_NAME_SIZE, MAX_NAME_SIZE + 32);

		// IV size is 16
		byte[] iv = Arrays.copyOfRange(packet, MAX_NAME_SIZE + hash.length, MAX_NAME_SIZE + hash.length + 16);
		from.setIv(iv);

		configureCipher(from, false);

		byte[] cipherText = Arrays.copyOfRange(packet, MAX_NAME_SIZE + hash.length + iv.length, packet.length);

		// check packet integrity, throws an exception if not satisfied
		requireIntegrity(iv, cipherText, hash, from.getMacKey());

		byte[] plainText = cipher.doFinal(cipherText);

		long timestamp = bytesToLong(Arrays.copyOf(plainText, Long.BYTES));

		byte[] msg = Arrays.copyOfRange(plainText, Long.BYTES + 1, plainText.length);

		message.setMessage(new String(msg));
		message.setTimestamp(timestamp);
		message.setRecipient(fromUser);

		return message;
	}

	private void requireIntegrity(byte[] iv, byte[] cipherText, byte[] hash, byte[] macKey) throws Exception {

		byte[] wanted = generateHMAC(iv, cipherText, macKey);

		if (!Arrays.equals(wanted, hash)) {
			throw new Exception("Hash of packet didn't match... MitM attack?");
		}

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
