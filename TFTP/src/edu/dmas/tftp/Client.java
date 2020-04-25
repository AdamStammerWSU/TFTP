package edu.dmas.tftp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client {

	private enum OP {
		RRQ(new byte[] { 0, 1 }), WRQ(new byte[] { 0, 2 }), DATA(new byte[] { 0, 3 }), ACK(new byte[] { 0, 4 }),
		ERROR(new byte[] { 0, 5 }), ZERO(new byte[] { 0 });

		private byte[] value;

		private OP(byte b[]) {
			value = b;
		}

		public byte[] code() {
			return value;
		}
	};

	private String address, transferMode;
	private boolean getting;
	private InetAddress host;

	public Client(String addr, boolean getting, String transferMode) {
		this.address = addr;
		this.transferMode = transferMode;
		this.getting = getting;

		try {
			host = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			System.out.println("Failed to resolve hostname.");
			TFTP.exit(false);
		}
	}

	public boolean requestFile(String source, String destination) {
		// this will be called from TFTP.java as the 'run' method
		// setup options and special variables beforehand with the initializer, then
		// call this method
		if (getting) {
			return requestFilePull(source, destination);
		} else {
			return requestFilePush(source, destination);
		}
	}

	private boolean requestFilePull(String source, String destination) {
		try {
			DatagramSocket socket = new DatagramSocket();

			// Test comment

			byte[] opcode = OP.RRQ.code();
			byte[] fname = source.getBytes();
			byte[] tmode = transferMode.getBytes();

			// this "should" concatenate all of the pieces together for the get request
			byte[] buf = RQconcat(opcode, fname, tmode);

			// debug check of the byte array, it matches wireshark capture
			System.out.println(bytesToHex(buf));

//			byte[] buf = new byte[4 + fname.length + tmode.length];
//			for (int i = 0; i < buf.length; i++) {
//				if (i < 2) {
//				} else if (i > 1 && i <= fname.length + 1) {
//				} else if (i == fname.length + 2) {
//				}
//			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true; // return true if succeeded, false if failed
	}

	private boolean requestFilePush(String source, String destination) {
		// open the local file

		// send

		return true; // return true if succeeded, false if failed
	}

	private byte[] RQconcat(byte[] opcode, byte[] fname, byte[] tmode) {
		byte[] c = concat(opcode, concat(fname, concat(OP.ZERO.code(), concat(tmode, OP.ZERO.code()))));
		return c;
	}

	private byte[] concat(byte[] a, byte[] b) {
		// returns a byte array of b concatenated onto the end of a
		// [a1, a2, a3, ..., an, b1, b2, b3, ..., bn]
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	// this purely a debug method. delete before turn in
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes();

	public static String bytesToHex(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}
}
