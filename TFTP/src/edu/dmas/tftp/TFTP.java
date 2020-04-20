package edu.dmas.tftp;

public class TFTP {

	static boolean helpPrinted = false;

	// public void requestFile(String addr, boolean getting, String
	// transferMode,String fileName) {
	public static void main(String[] args) {
		String host = "";
		boolean getting = true;
		String source = "", destination = "";

		if (args.length < 3) {
			// minimum of address, get/put, filename
			System.out.println("More information needed. Printing help (--?, --help)");
			printHelp();
		} else {
			//grab the host address
			host = args[0];
			
			//check for get/put
			if (args[1].equalsIgnoreCase("get")) {
				getting = true; // we are getting a file
			} else if (args[1].equalsIgnoreCase("put")) {
				getting = false; // so we are putting
			} else {
				//neither get nor put so exit with error after printing help
				System.out.println("Unspecified [GET | PUT]");
				printHelp();
				exit(false);
			}
		}
	}

	public static void printHelp() {

		// i think this is all we need

		if (!helpPrinted) {
			System.out.println("Usage: tftp [options] host [get/put] source [destination]");
			System.out.println("\t host -> \t IP Address of TFTP server host");
			System.out.println(
					"\t GET -> \t indicates that the client is pulling remote [source] file from the server and saving it to local [destination]");
			System.out.println(
					"\t PUT -> \t indicates that the client is pushing local [source] file to the server and saving it to remote [destination]");

			System.out.println("\t source -> \t the file to be transfered");
			System.out.println("\t destination -> \t where to transfer the file");

			System.out.println("\t [OPTIONS]   ---------");
			System.out.println("\t\t -i specefies binary mode (octet) [NOT IMPLEMENTED]");
			helpPrinted = true;
		}
	}

	public static void exit(boolean inProcess) {
		System.out.print("System Exiting");
		if (inProcess) {
			System.out.print(" while in process");
		}
		System.out.println("...");
		System.exit(0);
	}

}
