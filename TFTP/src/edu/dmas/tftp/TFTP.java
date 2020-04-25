package edu.dmas.tftp;

import java.util.regex.Pattern;

public class TFTP {

	// track help prints so it only gets printed once
	static boolean helpPrinted = false;

	// compile pattern checking for ipv4 address [xxx].[xxx].[xxx].[xxx]
	static Pattern p = Pattern.compile("\\d{1,3}+[.]\\d{1,3}+[.]\\d{1,3}+[.]\\d{1,3}+");

	// public void requestFile(String addr, boolean getting, String
	// transferMode,String fileName) {
	public static void main(String[] args) {
		String host = "";
		boolean getting = true;
		String source = "", destination = "";

		// check for any help request
		for (String s : args) {
			if (s.contentEquals("-h") || s.contentEquals("-?") || s.contentEquals("--help")) {
				printHelp();
				exit(false);
			}
			if (s.contentEquals("-i")) {
				System.out
						.println(" Binary Transfer ('-i') not yet implemented. Please try again without binary mode.");
				printHelp();
				exit(false);
			}
		}

		if (args.length < 3) {
			// minimum of host_address, get/put, filename
			System.out.println("More information needed. Printing help (--?, --help)");
			printHelp();
		} else if (args.length > 4) {
			System.out.println("Too many arguments supplied. Printing help (-?, -h, --help");
		} else {
			// grab the host address
			host = args[0];
			if (!ipv4FormatCheck(host)) {
				System.out.println("Host must be ipv4 address format [xxx].[xxx].[xxx].[xxx]");
				printHelp();
				exit(false);
			}

			// check for get/put
			if (args[1].equalsIgnoreCase("get")) {
				getting = true; // we are getting a file
			} else if (args[1].equalsIgnoreCase("put")) {
				getting = false; // so we are putting
			} else {
				// neither get nor put so exit with error after printing help
				System.out.println("Unspecified [GET | PUT]");
				printHelp();
				exit(false);
			}

			source = args[2];

			if (args.length >= 4) {
				destination = args[3];
			} else {
				destination = source;
			}
		}

		Client client = new Client(host, getting, "netascii"); // initialize client with netascii transfer mode
		if (destination.contentEquals(""))
			destination = source;

		if (getting)
			System.out.println("Requesting " + source + " from " + host + " as " + destination);
		else
			System.out.println("Pushing " + source + " to " + host + " as " + destination);

		if (client.requestFile(source, destination)) {
			System.out.println("Success!");
		}
	}

	public static void printHelp() {

		// i think this is all we need

		if (!helpPrinted) {
			System.out.print("\n\n\n");
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

	public static boolean ipv4FormatCheck(String address) {
		return p.matcher(address).matches();
	}

}
