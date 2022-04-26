import java.io.*;
import java.util.*;
import java.net.*;

public class BootstrapServer {

	static HashMap<Integer, String> data = new HashMap<Integer, String>();
	static int myID;
	static int predID;
	static int succID;
	static String myIP;
	static String predIP;
	static String succIP;
	static int myPort;
	static int predPort;
	static int succPort;
	static ServerSocket serverSocket;
	static int[] dataRange = { 0, 1023 };

	public static void main(String[] args) throws IOException {

		String fileName = args[0];
		FileInputStream fstream = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		myID = Integer.parseInt(br.readLine());
		myPort = Integer.parseInt(br.readLine());
		myIP = predIP = succIP = "localhost";
		predPort = succPort = myPort;
		predID = succID = myID;

		System.out.println("ID = " + myID + ", Port = " + myPort);

		String line = null;
		while ((line = br.readLine()) != null) {
			String[] lineSplit = line.split(" ");
			int dataID = Integer.parseInt(lineSplit[0]);
			String dataValue = lineSplit[1];
			data.put(dataID, dataValue);
			System.out.println(dataID + " " + dataValue);
		}

		System.out.println("\nBootstrap NameServer running");
		serverSocket = new ServerSocket(myPort);

		Thread Threads = new Threads();
		Threads.start();

		Thread ThreadB = new Threads.ThreadB();
		ThreadB.start();

	}

	static void printData() {
		for (int i = dataRange[0]; i <= dataRange[1]; i++) {
			if (data.containsKey(i)) {
				System.out.println("- " + i + " " + data.get(i));
			}
		}
	}
}
