package edu.dmas.tftp;

import java.awt.Frame;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Client {
	
	
	
	
	
	public Client() {
		//initializer
	}
	
	public void requestFile(String addr, String rType, String transferMode,String fileName) {
		//this will be called from TFTP.java as the 'run' method
		//setup options and special varibles beforehand, then call this method
		try {
			DatagramSocket socket = new DatagramSocket();
			
			//Test comment
			
			byte[] opcode;
			byte[] fname = fileName.getBytes();
			byte[] tmode = transferMode.getBytes();
			
			
			if(rType.equalsIgnoreCase("Get")) {
				opcode = new byte[] {0,1};
			}
			else {
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
			
			
			InetAddress address = InetAddress.getByName(addr);
			
			
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}

}
