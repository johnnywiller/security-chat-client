package br.furb.dss;

import java.util.Arrays;

public class ListeningServer extends Thread {

	private ServerSocket server;
	private final int MAX_BUF = 255;
	private MessageEncryptor encryptor; 
	
	public ListeningServer(ServerSocket server, MessageEncryptor encryptor) throws Exception {
		this.server = server;
		this.encryptor = encryptor;
	}
	
	@Override
	public void run() {
		try {
			listen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void listen() throws Exception {
		
		byte[] receivedPacket;
		
		while(true) {
			
			receivedPacket = new byte[MAX_BUF];
			
			server.getIn().read(receivedPacket);
						
			receivedPacket = getResizedPacket(receivedPacket);
			
			Message msg = decryptPacket(receivedPacket);
			
			System.out.println("received msg from [" + msg.getRecipient() + "] = " + msg.getMessage());
			
		}
		
	}
	
	private Message decryptPacket(byte[] packet) throws Exception {
						
		Message msg = encryptor.decryptMessage(packet);
		
		return msg;
	}
	
	private byte[] getResizedPacket(byte[] packet) {
		byte[] resized;
		byte size = packet[0];					
		
		resized = Arrays.copyOfRange(packet, 1, size + 1);		
		
		return resized;
	}
	
}
