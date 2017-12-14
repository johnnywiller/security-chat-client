package br.furb.dss;

import java.io.IOException;

public class ListeningServer extends Thread {

	private ServerSocket server;
	private final int MAX_BUF = 512;
	
	public ListeningServer(ServerSocket server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		
	}
	
	private void listen() throws IOException {
		
		byte[] receivedPacket;
		
		while(true) {
			
			receivedPacket = new byte[MAX_BUF];
			
			server.getIn().read(receivedPacket);
			
			byte[]
			
		}
		
	}
	
	
	
}
