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

		out.writeObject(signDHParameter(p));
		out.flush();

		out.writeObject(signDHParameter(g));
//		out.write(signDHParameter(g));
		out.flush();

		out.writeObject(signDHParameter(y));
//		out.write(signDHParameter(y));
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

	private DHParam signDHParameter(BigInteger dhParam) throws Exception {

		byte[] signedDH = new byte[385];

		byte[] param = dhParam.toByteArray();

		byte[] signature = Signer.getInstance().sign(param);

		System.arraycopy(signature, 0, signedDH, 0, signature.length);
		System.arraycopy(param, 0, signedDH, signature.length, param.length);

		DHParam dhparam = new DHParam();
		
		dhparam.setContent(param);
		dhparam.setSignature(signature);
		
		return dhparam;
		
	}

	private BigInteger verifyDHParametersSignature(ObjectInputStream in, byte[] pubKey) throws Exception {
		
//		System.out.println("verify DH parameters");
//		System.out.println(Arrays.toString(pubKey));
//		
		//byte[] packet = new byte[385];

		DHParam param = (DHParam) in.readObject();
		
		System.out.println("READ DH PARAM");
		
		//byte dhSize = packet[0];
		
		byte[] signature = param.getSignature();
		byte[] content = param.getContent();

		boolean trust = Signer.getInstance().verify(content, signature, pubKey);
		
		System.out.println("SIGNATURE OF DH PARAM " + (trust ? "MATCHES" : "DON'T MATCHES"));
		
		//if (trust)	
			return new BigInteger(content);
		//else
		//	throw new Exception("Verification of DH parameter's signature has failed, MitM attack?");
	}

	public byte[] computeDHSecretKey(DHPrivateKey privateKey, DHPublicKey publicKey) throws Exception {

		final KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
		keyAgreement.init(privateKey);
		keyAgreement.doPhase(publicKey, true);

		byte[] commonSecret = keyAgreement.generateSecret();

		return commonSecret;

	}

}
