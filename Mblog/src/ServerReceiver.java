import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerReceiver extends Thread {
Replica replica;
	
	public ServerReceiver(Replica replica) {
		this.replica = replica;
	}
	public void run()
	{
		ServerSocket listener = null;
        try {
        	listener = new ServerSocket(replica.replicas.get(replica.replicaId).serverSocketId);
        	System.out.println("Listening in server port" + replica.replicas.get(replica.replicaId).serverSocketId);
            while (true) {
            	 Socket connectionSocket = listener.accept();
                 BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                 String serverMessage = inFromServer.readLine();
     			 System.out.println("server message = " + serverMessage);
                 replica.serverMessages.add(serverMessage);
                 System.out.println(replica.serverMessages.size());
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
