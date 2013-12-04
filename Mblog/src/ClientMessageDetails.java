import java.net.Socket;



public class ClientMessageDetails{

	String message;
	String clientIP;
	int clientPortNumber;
	
	public ClientMessageDetails(String message, int clientPortNumber,String clientIP) {
		this.clientIP = clientIP;
		this.clientPortNumber = clientPortNumber;
		this.message = message;
	}

	public ClientMessageDetails(String clientMessage) {
		String [] messageParts = clientMessage.split("[|]");
		if(messageParts.length>3)
		{
			this.message = messageParts[0]+"|" + messageParts[1];
			this.clientIP = messageParts[2];
			this.clientPortNumber = Integer.parseInt(messageParts[3]);
		}
		else
		{
			this.message = messageParts[0];
			this.clientIP = messageParts[1];
			this.clientPortNumber = Integer.parseInt(messageParts[2]);
		}
	}
}
