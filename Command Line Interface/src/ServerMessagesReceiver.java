import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerMessagesReceiver extends Thread {
	int clientPortNumber;
	
	public ServerMessagesReceiver(int clientPortNumber) {
		this.clientPortNumber = clientPortNumber;
	}
	public void run()
	{
		ServerSocket listener = null;
        try {
        	listener = new ServerSocket(clientPortNumber);
        	char [] cbuf = new char[1000];
            while (true) {
            	  
            	 Socket connectionSocket = listener.accept();
                 BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                 inFromServer.read(cbuf);
     			 System.out.println("server message = " + new String(cbuf));
     			 inFromServer.close();
                 connectionSocket.close();
           }
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        }
        finally {
        	if (listener != null)
				try {
					listener.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
	}

}
