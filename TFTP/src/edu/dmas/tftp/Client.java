package edu.dmas.tftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Client {

	// setup enum for op codes
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
	private DatagramPacket inboundPacket;

	public Client(String addr, boolean getting, String transferMode) {
		// initialize local variables with arguments
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

			byte[] opcode = OP.RRQ.code(); // request op code
			byte[] fname = source.getBytes(); // get file name bytes
			byte[] tmode = transferMode.getBytes(); // get transfer mode bytes

			// this "should" concatenate all of the pieces together for the get request
			byte[] buf = RQconcat(opcode, fname, tmode);

			sendBuffer(socket, buf, 69); // send the request packet on port 69

			// now we need to receive the data until it is done
			boolean moreData = true;
			byte[] blockNumber = new byte[] { 0, 0 }; // which block are we on
			byte[] dataBlock = new byte[] { 0, 0 }; // what is in the datablock
			BufferedWriter writer = null; // writer for local file
			try {
				writer = new BufferedWriter(new FileWriter(destination)); // open up the writer
			} catch (IOException e) {
				System.out.println("Failed to open the destination file locally.");
				TFTP.exit(true);
			}
			while (moreData) {
				// read the next packet
				buf = receivePacket(socket);

				// split data by useful pieces
				opcode = Arrays.copyOfRange(buf, 0, 2);
				if (Arrays.equals(opcode, OP.DATA.code())) {
					// it is a data packet
					// grab the block number
					blockNumber = Arrays.copyOfRange(buf, 2, 4);

					// grab data out of the buffer
					dataBlock = Arrays.copyOfRange(buf, 4, buf.length);

					// check its size to determine if there is more data
					int dataLength = 0;
					String asciiBlock = ""; // convert the data to ascii for the file write
					for (int i = 0; i < dataBlock.length; i++) { // loop through each byte
						if (dataBlock[i] != (byte) 0b11111111) { // see if it's the 'null' character predefined in the
																	// read packet buffer
							dataLength++; // if it's not, increase the length count
							asciiBlock += (char) dataBlock[i]; // and add it as a character to the string buffer
						}
					}
					if (dataLength < 512) // check the size for more data indication
						moreData = false;

					// save the data to file
					try {
						writer.write(asciiBlock);
					} catch (IOException e) {
						System.out.println("Failed to write to local destination file.");
						TFTP.exit(true);
					}

					// and acknowledge that block
					sendBuffer(socket, concat(OP.ACK.code(), blockNumber), inboundPacket.getPort());
				} else {
					// send an error because we expected a data packet

					// we decided not to implement this because it's quite difficult to test without
					// a server we have complete control over, like one we coded ourselves. As such
					// we deemed it outside the scope of this assignment
					System.out.println("Error Received when ACK expected. Assume file is incomplete");
					System.out.println("Error: "); // indicate error
					byte[] errorMsgBuf = new byte[512]; // prepare to process the message
					// Arrays.fill(errorMsgBuf, (byte) -1);
					errorMsgBuf = Arrays.copyOfRange(buf, 4, buf.length); // copy the message over
					for (int i = 0; i < errorMsgBuf.length; i++) { // loop through the length of the message
						if (errorMsgBuf[i] != (byte) 0b11111111) // and print it out in readable characters
							System.out.print((char) errorMsgBuf[i]);
					}
					System.out.println();
					TFTP.exit(true); // close the program with exit message

				}

			}

			try {
				writer.close(); // close up the write buffer
			} catch (IOException e) {
				System.out.println("Failed to close the destination file writer");
				TFTP.exit(true);
			}

		} catch (SocketException e) {
			System.out.println("Error opening socket.");
			TFTP.exit(true);
		}

		return true; // return true if succeeded, false if failed
	}

	private boolean requestFilePush(String source, String destination) {

		// open the local file
		try {

			File Infile = new File(source);

			byte[] filecontent = new byte[(int) Infile.length()];// set the byte array to the length of the file in
																	// bytes

			FileInputStream InfileStr = new FileInputStream(Infile);// get the file as an input stream

			InfileStr.read(filecontent);// read in the files bytes

			// file is no longer needed as bytes are in array
			InfileStr.close();

			try {

				// request put
				DatagramSocket socket = new DatagramSocket();

				byte[] opcode = OP.WRQ.code(); // request op code
				byte[] fname = destination.getBytes(); // get file name bytes
				byte[] tmode = transferMode.getBytes(); // get transfer mode bytes
				byte[] curBlockNumber = new byte[] { 0, 0 }; // the current sent block number
				byte[] recBlockNumber;// block number received in ACK
				byte[] dataBuf;// data buffer
				int blockCount = 1;// current block number in int

				// this "should" concatenate all of the pieces together for the get request
				byte[] buf = RQconcat(opcode, fname, tmode);

				sendBuffer(socket, buf, 69); // send the request packet on port 69

				buf = receivePacket(socket);// Receive ACK

				// Get ACK info
				opcode = Arrays.copyOfRange(buf, 0, 2);
				recBlockNumber = Arrays.copyOfRange(buf, 2, 4);

				// check if ACK is correct then start sending data
				if (Arrays.equals(opcode, OP.ACK.code()) && Arrays.equals(recBlockNumber, curBlockNumber)) {
					// send

					// set up parameters for first send
					Boolean moreData = true;
					curBlockNumber = new byte[] { 0, (byte) blockCount };

					// Send loop
					while (moreData) {

						if (filecontent.length > 511) {
							dataBuf = new byte[512];// data buffer is 512 bytes
						} else {
							dataBuf = new byte[filecontent.length];// data buffer is less than 512 bytes
						}

						// Get opcode and data to send
						opcode = OP.DATA.code();
						System.arraycopy(filecontent, 0, dataBuf, 0, dataBuf.length);
						buf = concat(concat(opcode, curBlockNumber), dataBuf);

						// Send Data
						sendBuffer(socket, buf, inboundPacket.getPort());

						// Get ACK
						buf = receivePacket(socket);

						// Get Ack info
						opcode = Arrays.copyOfRange(buf, 0, 2);
						recBlockNumber = Arrays.copyOfRange(buf, 2, 4);

						// Check if Ack info is correct if not re-send
						if (Arrays.equals(opcode, OP.ACK.code()) && Arrays.equals(recBlockNumber, curBlockNumber)) {

							// if there is more data left get rid of last sent
							if (filecontent.length - 512 > 0) {
								byte[] copy = new byte[filecontent.length - 512];
								System.arraycopy(filecontent, 512, copy, 0, copy.length);
								filecontent = copy;
							} else {
								moreData = false;// if there is no more data, stop looping
							}

							blockCount++;
							if (blockCount > 65535) {// rollover currently not working in our tests
								blockCount = 0;
							}
							int curBlockNumberLeft = blockCount / 256;
							int curBlockNumberRight = blockCount % 256; // separate block number into 2 digits
							curBlockNumber = new byte[] { (byte) curBlockNumberLeft, (byte) curBlockNumberRight };
						} else {
							System.out.println(
									"Error Received when ACK expected, or ACK out of order. Assume file is incomplete");
							System.out.println("Error: "); // indicate error
							byte[] errorMsgBuf = new byte[512]; // prepare to process the message
							Arrays.fill(errorMsgBuf, (byte) -1);
							errorMsgBuf = Arrays.copyOfRange(buf, 4, buf.length); // copy the message over
							for (int i = 0; i < errorMsgBuf.length; i++) { // loop through the length of the message
								if (errorMsgBuf[i] != (byte) -1) // and print it out in readable characters
									System.out.print((char) errorMsgBuf[i]);
							}
							System.out.println();
							TFTP.exit(true); // close the program with exit message
						}
					}
				}

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				System.out.println("Error opening socket.");
				TFTP.exit(true);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Failed to read local source file.");
			TFTP.exit(true);
		}

		return true; // return true if succeeded, false if failed
	}

	private void sendBuffer(DatagramSocket socket, byte[] buf, int port) {
		try {
			if (port == -1) {
				port = inboundPacket.getPort();
			}
			socket.send(new DatagramPacket(buf, buf.length, host, port));
		} catch (IOException e) {
			System.out.println("Failed to send on socket.");
			TFTP.exit(true);
		}
	}

	private byte[] receivePacket(DatagramSocket socket) {
		// initialize packet that can be recognized as 'null'
		// it should never be sent anywhere
		byte[] buf = new byte[532];
		Arrays.fill(buf, (byte) 0b11111111); // new byte[1024];
		inboundPacket = new DatagramPacket(buf, buf.length, host, socket.getLocalPort());
		try {
			socket.setSoTimeout(10000);
			socket.receive(inboundPacket);
		} catch (SocketTimeoutException e) {
			System.out.println("Timeout receiving packet. Recovery not yet implemented.");
			TFTP.exit(true);
		} catch (IOException e) {
			System.out.println("Error receiving packet.");
			TFTP.exit(true);
		}
		return inboundPacket.getData();
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
