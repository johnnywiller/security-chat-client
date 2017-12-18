package br.furb.dss;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHPublicKeySpec;

public class DiffieHellmanUitls {

	public KeyPair generateKeyPair() throws NoSuchAlgorithmException {

		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
		keyPairGenerator.initialize(1024);

		return keyPairGenerator.generateKeyPair();

	}

	/**
	 * Pass DH material to the server
	 * 
	 * @param publicKey
	 *            our DH public key
	 * @param out
	 *            server socket stream
	 * @throws Exception
	 */
	public void passPublicToServer(DHPublicKey publicKey, ObjectOutputStream out) throws Exception {
		BigInteger p, g, y;

		p = publicKey.getParams().getP();
		g = publicKey.getParams().getG();
		y = publicKey.getY();

		out.write(signDHParameter(p));
		out.flush();
		
		out.write(signDHParameter(g));
		out.flush();
		
		out.write(signDHParameter(y));
		out.flush();
	}

	public DHPublicKey getServerPublic(ObjectInputStream in, byte[] pubKey) throws Exception {

		KeyFactory factory = KeyFactory.getInstance("DH");

		BigInteger p, g, y;

		p = verifyDHParametersSignature(in, pubKey);
		g = verifyDHParametersSignature(in, pubKey);
		y = verifyDHParametersSignature(in, pubKey);

		KeySpec spec = new DHPublicKeySpec(y, p, g);

		DHPublicKey publicKey = (DHPublicKey) factory.generatePublic(spec);

		return publicKey;
	}

	private byte[] signDHParameter(BigInteger dhParam) throws Exception {

		byte[] signedDH = new byte[385];

		byte[] param = dhParam.toByteArray();

		byte[] signature = Signer.getInstance().sign(param);

		System.arraycopy(signature, 0, signedDH, 0, signature.length);
		System.arraycopy(param, 0, signedDH, signature.length, param.length);

		return signedDH;
	}

	private BigInteger verifyDHParametersSignature(ObjectInputStream in, byte[] pubKey) throws Exception {

		byte[] packet = new byte[385];

		in.read(packet);
		
		//byte dhSize = packet[0];
		
		byte[] signature = Arrays.copyOf(packet, 256);
		byte[] content = Arrays.copyOfRange(packet, 256, packet.length);

		boolean trust = Signer.getInstance().verify(content, signature, pubKey);

		if (trust)
			return new BigInteger(content);
		else
			throw new Exception("Verification of DH parameter's signature has failed, MitM attack?");
	}

	public byte[] computeDHSecretKey(DHPrivateKey privateKey, DHPublicKey publicKey) throws Exception {

		final KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
		keyAgreement.init(privateKey);
		keyAgreement.doPhase(publicKey, true);

		byte[] commonSecret = keyAgreement.generateSecret();

		return commonSecret;

	}

}
