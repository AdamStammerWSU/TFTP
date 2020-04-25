package edu.dmas.tftp;

import java.awt.Frame;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Client {
	
	private String host, transferMode;
	private boolean getting;
	
	
	
	public Client(String addr, boolean getting, String transferMode) {
		this.host = addr;
		this.transferMode = transferMode;
		this.getting = getting;
	}
	
	public boolean requestFile(String source, String destination) {
		//this will be called from TFTP.java as the 'run' method
		//setup options and special variables beforehand with the initializer, then call this method
		if(getting) {
			return requestFilePull(source, destination);
		} else {
			return requestFilePush(source, destination);
		}
	}
	
	private boolean requestFilePull(String source, String destination) {
		try {
			DatagramSocket socket = new DatagramSocket();
			
			//Test comment
			
			byte[] opcode;
			byte[] fname = source.getBytes();
			byte[] tmode = transferMode.getBytes();
			
			
			if(getting) {
				opcode = new byte[] {0,1};
			}
			else { //putting a file instead of getting
				opcode = new byte[]{0,2};
			}
			
			
			byte[] buf = new byte[4 + fname.length + tmode.length];
			
			for(int i =0; i < buf.length; i++) {
				
				if(i < 2) {
					
				}
				else if(i > 1 && i <= fname.length + 1) {
					
				}
				else if(i == fname.length + 2) {
					
					
				}
				
			}
			
			InetAddress address = InetAddress.getByName(host);
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true; //return true if succeeded, false if failed
	}
	
	private boolean requestFilePush(String source, String destination) {
		//open the local file
		
		//send 
		
		return true; //return true if succeeded, false if failed
	}

}
