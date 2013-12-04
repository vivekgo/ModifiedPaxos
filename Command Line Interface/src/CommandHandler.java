import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;


public class CommandHandler extends Thread {

	String command = "";
	
	private BufferedReader in;
	private DataOutputStream out;
	Socket clientSocket;
	int serverPortNumber;
	String serverIP;
	int clientPortNumber;
	String clientIP;
	public CommandHandler(String command, int serverPostNumber, String serverIP,int clientPortNumber, String clientIP ) {
		this.command = command;
		this.serverPortNumber =serverPostNumber;
		this.serverIP = serverIP;
		this.clientIP = clientIP;
		this.clientPortNumber = clientPortNumber;
				
	}

	@Override
	public void run() {
		handleCommand(command);
	}
	

	// post, read, fail and unfail
	private void handleCommand(String command) {
		String lowerCaseCommand = command.toLowerCase();
		if (lowerCaseCommand.startsWith("post")) {
			String [] commandParts = command.split("\\(");
			commandParts[1] = commandParts[1].replace(")","").trim().replace("\"", "");
			sendToServer("post|" + commandParts[1]+"|"+clientIP+"|"+clientPortNumber);
		} else if (lowerCaseCommand.startsWith("read")) {
			sendToServer("read"+"|"+clientIP+"|"+clientPortNumber);
		} else if (lowerCaseCommand.startsWith("fail")) {
			sendToServer("fail"+"|"+clientIP+"|"+clientPortNumber);
		} else if (lowerCaseCommand.startsWith("unfail")) {
			sendToServer("unfail"+"|"+clientIP+"|"+clientPortNumber);
		}
	}
	
	private void sendToServer (String message)
	{
		
		try {
	        String replyFromServer;
	        Inet4Address serverIPAddess = (Inet4Address) Inet4Address.getByName(serverIP);
	        clientSocket = new Socket(serverIPAddess, this.serverPortNumber);
	        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	        outToServer.writeBytes(message + "\n");
	        clientSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
