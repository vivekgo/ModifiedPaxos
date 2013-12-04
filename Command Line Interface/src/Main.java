import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int serverPortNumber = 5002;
		String serverAddress = "127.0.0.1";
		int clientPortNumber = 6002;
		String clientAddress = "127.0.0.1";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));

			String input = "";
			ServerMessagesReceiver receiver = new ServerMessagesReceiver(clientPortNumber);
			receiver.start();
			while ((input = br.readLine()) != null) {
				CommandHandler handler = new CommandHandler(input,serverPortNumber,serverAddress, clientPortNumber,  clientAddress);
				handler.start();
			}
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

}
