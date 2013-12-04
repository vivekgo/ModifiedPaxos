import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ClientReceiver extends Thread {

	Replica replica;
	
	public ClientReceiver(Replica replica) {
		this.replica = replica;
	}
	public void run()
	{
		ServerSocket listener = null;
        try {
        	listener = new ServerSocket(Replica.replicas.get(replica.replicaId).clientSocketId);
            while (true) {
            	 Socket connectionSocket = listener.accept();
                 BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                 String clientMessage = inFromClient.readLine();
     			 System.out.println("client message = " + clientMessage);
                 ClientMessageDetails handler = new ClientMessageDetails(clientMessage);
                 replica.clientMessages.add(handler);
                 System.out.println(replica.clientMessages.size());
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
