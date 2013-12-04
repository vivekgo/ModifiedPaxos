import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;

import sun.misc.Cleaner;

public class ClientMessageHandler extends Thread {

	Replica replica;

	public ClientMessageHandler(Replica replica) {
		this.replica = replica;
	}

	public void run() {
		while (true) {
			ClientMessageDetails currentMessage = null;
			synchronized (replica.clientMessages) {
				currentMessage = replica.clientMessages.poll();
			}
			if (currentMessage != null) {
				System.out.println(currentMessage.message);
				
				if (!replica.isFailed
						|| (currentMessage.message.startsWith("unfail") 
								&& replica.isFailed)) {
					handleClientMessage(currentMessage);
				}
				else 
				{
			    	replyToClient(currentMessage, "FAIL");
				}
			} else {
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	// Message parts are "fail" or "unfail" or "read" or "post|Message Body"
	public void handleClientMessage(ClientMessageDetails currentMessage) {
		String[] messageParts;
		if(currentMessage.message.contains("|"))
		{
			messageParts = currentMessage.message.split("[|]");
		}
		else
		{
			messageParts =  new String[1];
			messageParts[0] = currentMessage.message;
		}

		switch (messageParts[0]) {
		case "post":
			System.out.println("Starting post handling");
			String valueToPost = messageParts[1];
			//Check for next log position from log arraylist
			//check in hashmap for active paxos instance with this id
			//if not found create a new paxos instance with this id
			Paxos testPaxos;
			int numEntries = this.replica.paxosEntries.size();
			if (numEntries == 0
					|| (this.replica.paxosEntries.get(numEntries - 1)).isDecided){
				this.replica.paxosEntries.add(new Paxos(numEntries,this.replica.replicaId,valueToPost,this.replica.logger));
				testPaxos = this.replica.paxosEntries.get(numEntries);
				
				//set this.proposer = true; iamProposer()
			    testPaxos.iamProposer();
			    System.out.println("Send prepare is called");
			    testPaxos.sendPrepare();
			    
			    /*while (!testPaxos.isDecided)
			    {
			    	System.out.println("Value is not decided");
			    	try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }*/
			    if(testPaxos.isDecided && testPaxos.logEnrties.contains(valueToPost))
			    {
			    	replyToClient(currentMessage, "SUCCESS");
			    }
			    else
			    {
			    	if(!testPaxos.isDecided)
			    	{
			    		replica.paxosEntries.remove(testPaxos);
			    	}
			    	replyToClient(currentMessage, "FAIL");
			    }
			}
			else {
				testPaxos = this.replica.paxosEntries.get(numEntries -1);
				testPaxos.logEnrties.add(valueToPost);
				replyToClient(currentMessage, "SUCCESS");
			}
			
		    
			
			break;
		case "read":
			String logEntries = getLog();
			replyToClient(currentMessage, logEntries);
			break;
		case "fail":
			replica.isFailed = true;
			replyToClient(currentMessage, "Server is down");
			break;
		case "unfail":
			replica.isFailed = false;
			replica.isRecovered = false;
			int numberOfEntries = replica.paxosEntries.size()-1;
			int highestDecidedIndex = 0 ;
			if(numberOfEntries == -1) {
				highestDecidedIndex = -1;
			}
			if(numberOfEntries >= 0){
				if(replica.paxosEntries.get(numberOfEntries).isDecided)
				{
					highestDecidedIndex = numberOfEntries - 1;
					replica.paxosEntries.remove(numberOfEntries);
				}
				else
				{
					//remove entries
					
				    if(numberOfEntries == 0) {
				    	highestDecidedIndex = numberOfEntries-1;
				    	replica.paxosEntries.remove(0);
				    }
				    else {
				    	replica.paxosEntries.remove(numberOfEntries);
				    	replica.paxosEntries.remove(numberOfEntries - 1);
				    	highestDecidedIndex = numberOfEntries - 2;
				    }
				}
			}
			MessageCommunication.sendRecover(this.replica.replicaId, highestDecidedIndex);
			while(!replica.isRecovered)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			replyToClient(currentMessage, "Server is up back");
			
			break;
		default:
			break;

		}
	}
/*
	public String getLog() {
		StringBuilder logEntries = new StringBuilder();
		for (Paxos paxos : replica.paxosEntries) {
			if (paxos.acceptedValue != null)
				logEntries.append(paxos.acceptedValue + "\n");
		}
		return logEntries.toString();
	}
*/
	public String getLog() {
		StringBuilder logEntries = new StringBuilder();
		for(Paxos paxos : this.replica.paxosEntries) {
			if(paxos.isDecided) {
				for(String x: paxos.logEnrties)
				logEntries.append(x + "\n");
			}

		}
		return logEntries.toString();
	}
	
	public void replyToClient(ClientMessageDetails clientMessage,
			String replyMessage) {
		try {
			Inet4Address clientAddress = (Inet4Address) Inet4Address.getByName(clientMessage.clientIP);
			Socket clientSocket = new Socket(clientAddress, clientMessage.clientPortNumber);
			DataOutputStream outToClient = new DataOutputStream(
					clientSocket.getOutputStream());
			outToClient.write( replyMessage.getBytes());
			System.out.println("client reply = " + replyMessage);
			outToClient.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
