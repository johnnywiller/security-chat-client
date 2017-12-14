package br.furb.dss;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

	public void sendEncryptedMessage(Message message) throws Exception {

		// get user keys
		DestinationUser dest = ClientsKeyStore.getInstance().getUser(message.getRecipient());

		// we don't have made a handshake with this client yet
		// so we need to establish a session
		if (dest == null) {
			dest = ClientSessionInitiation.getInstance().startSession(message.getRecipient(), true);
		}

		// sets cipher to destination user
		configureCipher(dest, true);

		byte[] iv = cipher.getIV();

		byte[] timestamp = cipher.update(longToBytes(message.getTimestamp()));
		byte[] cipherText = cipher.doFinal(message.getMessage().getBytes());

		dest.setIv(cipher.getIV());

		// copy IV and cipher to a new array to send over the network
		byte[] packet = new byte[cipherText.length + iv.length + 1];
		System.arraycopy(iv, 0, packet, 1, iv.length);
		System.arraycopy(cipherText, 0, packet, iv.length + 1, cipherText.length);

		// set the packet size
		packet[0] = ((byte) (packet.length - 1));

		// send packet to server
		server.getOut().write(packet);
		server.getOut().flush();

		System.out.println("Message sent");
	}

	public Message decryptMessage(byte[] packet, String fromUser) throws Exception {

		Message message = new Message();

		// get user keys
		DestinationUser from = ClientsKeyStore.getInstance().getUser(fromUser);

		// we don't have made a handshake with this client yet
		// so we need to establish a session
		if (from == null) {
			throw new Exception("Client isn't in the keystore");
		}

		byte[] iv = Arrays.copyOf(packet, 16);
		from.setIv(iv);

		configureCipher(from, false);

		byte[] plainText = cipher.doFinal(packet);

		long timestamp = bytesToLong(Arrays.copyOf(plainText, Long.BYTES));

		byte[] msg = Arrays.copyOfRange(plainText, iv.length + Long.BYTES, packet.length);

		message.setMessage(new String(msg));
		message.setTimestamp(timestamp);

		return message;
	}

	private Cipher configureCipher(DestinationUser dest, boolean encrypt)
			throws InvalidKeyException, InvalidAlgorithmParameterException {

		byte[] iv = dest.getIv();
		SecretKeySpec secretKeySpec = new SecretKeySpec(dest.getSymmetricKey(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		cipher.init(encrypt ? Cipher.ENCRYPT_MODE : cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

		return cipher;
	}

	private void updateDestIV(DestinationUser dest, byte[] iv) {
		dest.setIv(iv);
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
