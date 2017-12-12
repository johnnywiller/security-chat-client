package br.furb.dss;

public class DestinationUser {

	private String name;
	private byte[] publicKey;
	private byte[] symmetricKey;
	private byte[] macKey;
	private byte[] iv;
	
	public byte[] getIv() {
		return iv;
	}
	public void setIv(byte[] iv) {
		this.iv = iv;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}
	public byte[] getSymmetricKey() {
		return symmetricKey;
	}
	public void setSymmetricKey(byte[] symmetricKey) {
		this.symmetricKey = symmetricKey;
	}
	public byte[] getMacKey() {
		return macKey;
	}
	public void setMacKey(byte[] macKey) {
		this.macKey = macKey;
	}

}
