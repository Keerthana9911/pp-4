import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

class Threads extends Thread {
	static class ThreadB extends Thread {
		private Socket socket;
		private DataInputStream dis;
		private DataOutputStream dos;
	
		public ThreadB() {
	
		}
	
		@Override
		public void run() {
	
			try {
				while (true) {
					socket = null;
					socket = BootstrapServer.serverSocket.accept();
	
					dis = new DataInputStream(socket.getInputStream());
					dos = new DataOutputStream(socket.getOutputStream());
					startProccess();
				}
			} catch (IOException e) {
	
				e.printStackTrace();
			}
	
		}
	
		private void startProccess() throws IOException {
			String command = null;
	
			command = dis.readUTF();
	
			switch (command) {
				case "enter":
					System.out.println("connection accepted");
					enter();
					System.out.print("> ");
					break;
				case "getData":
					System.out.println("sending Data");
					giveDataEntery();
	
					System.out.print("> ");
					break;
				case "updateInfo":
					System.out.println("updating info");
					UpdateInfoEntery();
	
					System.out.print("> ");
					break;
				case "predExit":
					System.out.println("Predecessor Exiting..");
					System.out.println("Getting Data ..");
					predExit();
	
					System.out.print("> ");
					break;
				case "succExit":
					System.out.println("Successor Exists");
					succExit();
	
					System.out.print("> ");
					break;
				case "queryResponse":
					System.out.println("Receiving");
					queryResponse();
	
					System.out.print("-> ");
					break;
				default:
					System.out.println("Invalid command");
					break;
			}
	
			socket.close();
			dis.close();
			dos.close();
		}
	
		private void queryResponse() {
	
			try {
	
				String Message = dis.readUTF();
				String IDs = dis.readUTF();
	
				System.out.println(Message);
	
				System.out.print(IDs);
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		private void enter() {
	
			int newID = 0, newPort = 0;
			String newIP = null;
	
			try {
	
				newID = dis.readInt();
				newIP = dis.readUTF();
				newPort = dis.readInt();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			if (BootstrapServer.dataRange[0] == 0 && BootstrapServer.dataRange[1] == 1023)
				enterOnlyBns(newID, newIP, newPort);
			else
				findSpotNS(newID, newIP, newPort);
		}
	
		private void enterOnlyBns(int newID, String newIP, int newPort) {
			try {
				System.out.println("newID: " + newID + ", newIP: " + newIP + ", newPort: " + newPort);
	
				int newStartRange = BootstrapServer.dataRange[0], newEndRange = newID - 1;
				BootstrapServer.succID = newID;
				BootstrapServer.predID = newID;
				BootstrapServer.succIP = newIP;
				BootstrapServer.predIP = newIP;
				BootstrapServer.succPort = newPort;
				BootstrapServer.predPort = newPort;
				BootstrapServer.dataRange[0] = newID;
				BootstrapServer.dataRange[1] = 1023;
	
				dos.writeInt(BootstrapServer.myID);
				dos.writeInt(BootstrapServer.myID);
	
				dos.writeUTF(BootstrapServer.myIP);
				dos.writeUTF(BootstrapServer.myIP);
	
				dos.writeInt(BootstrapServer.myPort);
				dos.writeInt(BootstrapServer.myPort);
	
				dos.writeInt(newStartRange);
				dos.writeInt(newEndRange);
	
				dos.writeUTF("   ID: " + BootstrapServer.myID + "\n");
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		private void findSpotNS(int newID, String newIP, int newPort) {
			try {
				System.out.println("newID: " + newID + ", newIP: " + newIP + ", newPort: " + newPort);
				boolean found = false;
				String IDs;
				int newPredPort = 0, newSuccPort = 0, nextPort = BootstrapServer.succPort;
				String newPredIP = "", newSuccIP = "", nextIP = BootstrapServer.succIP;
				int newPredID = 0, newSuccID = 0, nextID = BootstrapServer.succID;
	
				IDs = "   ID: " + BootstrapServer.myID + "\n";
				int newStartRange = 0, newEndRange = 0;
	
				while (!found) {
	
					IDs += "   ID: " + nextID + "\n";
					Socket tSocket = new Socket(nextIP, nextPort);
					DataInputStream tDis = new DataInputStream(tSocket.getInputStream());
					DataOutputStream tDos = new DataOutputStream(tSocket.getOutputStream());
	
					tDos.writeUTF("askRange");
					tDos.writeInt(newID);
					found = tDis.readBoolean();
	
					if (found) {
	
						newSuccID = nextID;
						newSuccIP = nextIP;
						newSuccPort = nextPort;
	
						newPredID = tDis.readInt();
						newPredIP = tDis.readUTF();
						newPredPort = tDis.readInt();
	
						tSocket.close();
						tDis.close();
						tDos.close();
	
						break;
					} else {
	
						nextID = tDis.readInt();
						nextIP = tDis.readUTF();
						nextPort = tDis.readInt();
	
					}
	
					if (nextID == BootstrapServer.myID) {
	
						newSuccID = BootstrapServer.myID;
						newSuccIP = BootstrapServer.myIP;
						newSuccPort = BootstrapServer.myPort;
	
						newPredID = BootstrapServer.predID;
						newPredIP = BootstrapServer.predIP;
						newPredPort = BootstrapServer.predPort;
	
						tSocket.close();
						tDis.close();
						tDos.close();
						found = true;
					}
				}
	
				if (!found) {
					System.out.println("Couldn't find");
					return;
				}
	
				newStartRange = newPredID;
				newEndRange = newID - 1;
	
				dos.writeInt(newSuccID);
				dos.writeInt(newPredID);
	
				dos.writeUTF(newSuccIP);
				dos.writeUTF(newPredIP);
	
				dos.writeInt(newSuccPort);
				dos.writeInt(newPredPort);
	
				dos.writeInt(newStartRange);
				dos.writeInt(newEndRange);
	
				dos.writeUTF(IDs);
	
				System.out.println("Found between " + newPredID + " and " + newSuccID);
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		private void predExit() {
			try {
				BootstrapServer.predID = dis.readInt();
				BootstrapServer.predIP = dis.readUTF();
				BootstrapServer.predPort = dis.readInt();
				BootstrapServer.dataRange[0] = dis.readInt();
	
				String getData = dis.readUTF();
				if (!getData.equals("")) {
					String[] splitData = getData.split(" ");
	
					int key;
					String value;
					for (int i = 0; i < splitData.length; i++) {
						key = Integer.valueOf(splitData[i]);
						i++;
						value = splitData[i];
						BootstrapServer.data.put(key, value);
					}
				}
				BootstrapServer.printData();
				System.out.println("New predecessor is: " + BootstrapServer.predID);
			} catch (IOException e) {
	
				e.printStackTrace();
			}
		}
	
		private void succExit() {
			try {
				BootstrapServer.succID = dis.readInt();
				BootstrapServer.succIP = dis.readUTF();
				BootstrapServer.succPort = dis.readInt();
				System.out.println("New successor is: " + BootstrapServer.succID);
			} catch (IOException e) {
	
				e.printStackTrace();
			}
		}
	
		private void UpdateInfoEntery() {
	
			try {
				BootstrapServer.succID = dis.readInt();
				BootstrapServer.succIP = dis.readUTF();
				BootstrapServer.succPort = dis.readInt();
				System.out.println("-> New successor is: " + BootstrapServer.succID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		private void giveDataEntery() {
	
			int[] newDataRange = new int[2];
			try {
	
				newDataRange[0] = dis.readInt();
				newDataRange[1] = dis.readInt();
	
				BootstrapServer.predID = dis.readInt();
				BootstrapServer.predIP = dis.readUTF();
				BootstrapServer.predPort = dis.readInt();
	
				String sendData = "";
				for (int i = newDataRange[0]; i <= newDataRange[1]; i++) {
					if (BootstrapServer.data.containsKey(i)) {
						sendData += i + " " + BootstrapServer.data.get(i) + " ";
	
						BootstrapServer.data.remove(i);
					}
				}
	
				dos.writeUTF(sendData);
				BootstrapServer.dataRange[0] = newDataRange[1] + 1;
	
				BootstrapServer.printData();
				System.out.println("-> New predecessor is: " + BootstrapServer.predID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Scanner scanner;

	public Threads() {

	}

	@Override
	public void run() {

		while (true) {
			scanner = new Scanner(System.in);

			String command = null;
			String keyStr = null;
			String value = null;
			int key = -1;
			int commandLength;
			System.out.print("\n> ");
			command = scanner.nextLine();

			if (command.contains(" ")) {
				String[] splittedCommand = command.split(" ");
				commandLength = splittedCommand.length;
				command = splittedCommand[0];
				if (commandLength > 1) {
					keyStr = splittedCommand[1];
					key = Integer.valueOf(keyStr);
				}
				if (commandLength == 3)
					value = splittedCommand[2];
			}

			switch (command) {
				case "lookup":
					findQueryRange(command, key, "");
					lookUp(command, key);
					break;
				case "insert":
					findQueryRange(command, key, value);
					insert(command, key, value);
					break;
				case "delete":
					findQueryRange(command, key, "");
					delete(command, key);
					break;
				case "print":
					System.out.println("NS Update: succID: " + BootstrapServer.succID + ", predID: " + BootstrapServer.predID + ", succIP: "
							+ BootstrapServer.succIP + ", predIP: " + BootstrapServer.predIP);
					System.out.println("NS Update: succPort: " + BootstrapServer.succPort + ", predPort: " + BootstrapServer.predPort
							+ ", startRange: " + BootstrapServer.dataRange[0] + ", endRange: " + BootstrapServer.dataRange[1]);
					BootstrapServer.printData();
					break;
				default:
					System.out.println("*Invalid Input ..");
					break;
			}
		}
	}

	private void findQueryRange(String command, int key, String value) {

		if (key >= BootstrapServer.dataRange[0] && key <= BootstrapServer.dataRange[1]) {
			String Message = "";
			String IDs = "   ID: " + BootstrapServer.myID + "\n";
			if (command.equals("insert")) {
				BootstrapServer.data.put(key, value);
				Message = "key inserted successfully at" + BootstrapServer.myID;
			} else if (BootstrapServer.data.containsKey(key)) {
				if (command.equals("lookup")) {
					Message = "key found"+ BootstrapServer.myID + ": " + BootstrapServer.data.get(key);
				} else if (command.equals("delete")) {
					BootstrapServer.data.remove(key);
					Message = "key deleted at" + BootstrapServer.myID;
				}

			} else
				Message = "key not found at" + BootstrapServer.myID;

			System.out.println(Message);
			System.out.println("Servers:");
			System.out.print(IDs);

		} else {
			try {
				Socket socket = new Socket(BootstrapServer.succIP, BootstrapServer.succPort);
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				dos.writeUTF("query");

				dos.writeUTF(command);
				dos.writeInt(key);
				dos.writeUTF(value);
				dos.writeUTF("ID:" + BootstrapServer.myID + "\n");

				socket.close();
				dis.close();
				dos.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void lookUp(String command, int key) {

	}

	private void insert(String command, int key, String value) {

	}

	private void delete(String command, int key) {

	}
}