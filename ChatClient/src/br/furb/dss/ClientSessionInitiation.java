package br.furb.dss;

import java.io.IOException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

public class ClientSessionInitiation {

	private DiffieHellmanUitls dh = new DiffieHellmanUitls();
	private static ServerSocket server = new ServerSocket();
	private static ClientSessionInitiation instance;

	public static ClientSessionInitiation getInstance() {

		if (instance == null)
			instance = new ClientSessionInitiation(server);

		return instance;

	}

	private ClientSessionInitiation(ServerSocket server) {
		this.server = server;
	}

	public DestinationUser startSession(String user, boolean isInitialApplicant) throws Exception {

		DestinationUser dest = new DestinationUser();

		// verifies if is this user starting the DH process or is only handling a
		// previous started DH from another user
		if (isInitialApplicant) {
			initiateKex(user);
		} else {
			ackKex(user);
		}

		DHPublicKey publicKey;
		KeyPair keyPair;
		byte[] secret;

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
		byte[] symmetricKey = Arrays.copyOf(secret, 16);
		byte[] macKey = Arrays.copyOfRange(secret, 16, 32);

		dest.setSymmetricKey(symmetricKey);
		dest.setMacKey(macKey);

		byte[] iv = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);

		dest.setIv(iv);

		// add user to keystore
		ClientsKeyStore.getInstance().addUser(dest);

		return dest;
	}

	private void initiateKex(String value) throws IOException {

		String msg = "/startsession " + value;

		byte[] packet = new byte[msg.length() + 1];

		packet[0] = (byte) msg.length();

		System.arraycopy(msg.getBytes(), 0, packet, 1, msg.getBytes().length);

		server.getOut().write(packet);
		server.getOut().flush();

	}
	
	private void ackKex(String value) throws IOException {
		
		String msg = "/acksession " + value;

		byte[] packet = new byte[msg.length() + 1];

		packet[0] = (byte) msg.length();

		System.arraycopy(msg.getBytes(), 0, packet, 1, msg.getBytes().length);

		server.getOut().write(packet);
		server.getOut().flush();
		
	}

}
