import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMessageHandler extends Thread {
	Replica replica;

	public ServerMessageHandler(Replica replica) {
		this.replica = replica;
	}

	public void run() {
		while (true) {
			String serverMessage = null;
			synchronized (replica.serverMessages) {
				serverMessage = replica.serverMessages.poll();
			}
			if (serverMessage != null) {
				System.out.println(serverMessage);

				if (!replica.isFailed) {
					handleServerMessage(serverMessage);
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
    public void sendMessage() {
    	
    }
	public void handleServerMessage(String currentMessage) {
		String[] messageParts = currentMessage.split("[|]");
		int senderReplicaId = Integer.parseInt(messageParts[1]);
		int paxosId = Integer.parseInt(messageParts[2]);
		int numEntries = this.replica.paxosEntries.size();
		Paxos currentPaxos;
		
		switch (Integer.parseInt(messageParts[0])) {
		case 1:
			BallotPair proposedBallotNumPair = new BallotPair(Integer.parseInt(messageParts[4]), Integer.parseInt(messageParts[3]));
			//receivePrepareMsg
			this.replica.logger.write("Received Prepare Msg for Paxos Instance: " + String.valueOf(paxosId) );
			if(paxosId > numEntries - 1) {
				//Create a new one
				this.replica.paxosEntries.add(new Paxos(paxosId,this.replica.replicaId,"",this.replica.logger));
				currentPaxos = this.replica.paxosEntries.get(numEntries);
				currentPaxos.onreceivePrepare(proposedBallotNumPair, senderReplicaId);
			}
			else if( paxosId == numEntries - 1) {
				//check if it is active or not
				currentPaxos = this.replica.paxosEntries.get(numEntries - 1);
				//active and join it - else already decided and send decide
				if(!currentPaxos.isDecided) {
					currentPaxos.onreceivePrepare(proposedBallotNumPair, senderReplicaId);
				}
				else {
					MessageCommunication.sendDecideUnicast(this.replica.replicaId,senderReplicaId, paxosId, currentPaxos.logEnrties);
				}
			}
			else {
				//already decided
				currentPaxos = this.replica.paxosEntries.get(numEntries - 1);
				MessageCommunication.sendDecideUnicast(this.replica.replicaId,senderReplicaId, paxosId, currentPaxos.logEnrties);
			}
			break;
		case 2:
			//receiveAcktoPrepareMsg
			this.replica.logger.write("Received AckToPrepare Msg for Paxos Instance: " + String.valueOf(paxosId) );
			//Ongoing paxos Instance
			if(paxosId == numEntries - 1 && !this.replica.paxosEntries.get(paxosId).isDecided) {
				currentPaxos = this.replica.paxosEntries.get(paxosId);
				BallotPair ownBallotNumPair = new BallotPair(Integer.parseInt(messageParts[4]), Integer.parseInt(messageParts[3]));
				BallotPair acceptedBallotNumPair = new BallotPair(Integer.parseInt(messageParts[6]), Integer.parseInt(messageParts[5]));
				currentPaxos.onreceiveAckToPrepare(ownBallotNumPair, acceptedBallotNumPair,messageParts.length>7?messageParts[7]:"");
			}
			else {
				if(paxosId < numEntries - 1) {
					MessageCommunication.sendDecideUnicast(this.replica.replicaId, senderReplicaId, paxosId, this.replica.paxosEntries.get(paxosId).logEnrties);
					this.replica.logger.write("Responded to  AckToPrepare Msg with Decide Msg for Paxos Instance: " + String.valueOf(paxosId) );
				}
				else {
					this.replica.logger.write("Ignoring AckToPrepare Msg for Paxos Instance: " + String.valueOf(paxosId) );
				}
			}			
			break;
		case 3:
			//receiveNAcktoPrepareMsg
			this.replica.logger.write("Received NAckToPrepare Msg for Paxos Instance: " + String.valueOf(paxosId) );
			//Ongoing paxos Instance
			if(paxosId == numEntries - 1 && !this.replica.paxosEntries.get(paxosId).isDecided) {
				currentPaxos = this.replica.paxosEntries.get(paxosId);
				BallotPair ownBallotNumPair = new BallotPair(Integer.parseInt(messageParts[4]), Integer.parseInt(messageParts[3]));
				currentPaxos.onreceiveNAckToPrepare(ownBallotNumPair);
			}
			else {
				if(paxosId < numEntries - 1) {
					MessageCommunication.sendDecideUnicast(this.replica.replicaId, senderReplicaId, paxosId, this.replica.paxosEntries.get(paxosId).logEnrties);
					this.replica.logger.write("Responded to  NAckToPrepare Msg with Decide Msg for Paxos Instance: " + String.valueOf(paxosId) );
				}
				else {
					this.replica.logger.write("Ignoring NAckToPrepare Msg for Paxos Instance: " + String.valueOf(paxosId) );
				}
			}			
			break;
		case 4:
			//onreceiveAccept
			BallotPair acceptBallotNumPair = new BallotPair(Integer.parseInt(messageParts[4]), Integer.parseInt(messageParts[3]));
			this.replica.logger.write("Received Accept Msg for Paxos Instance: " + String.valueOf(paxosId) );
			if(paxosId > numEntries - 1) {
				//Create a new one
				this.replica.paxosEntries.add(new Paxos(paxosId,this.replica.replicaId,"",this.replica.logger));
				currentPaxos = this.replica.paxosEntries.get(numEntries);
				currentPaxos.onreceiveAccept(acceptBallotNumPair,messageParts[5]);
			}
			else if( paxosId == numEntries - 1) {
				//check if it is active or not
				currentPaxos = this.replica.paxosEntries.get(numEntries - 1);
				//active and join it - else already decided and send decide
				if(!currentPaxos.isDecided) {
					currentPaxos.onreceiveAccept(acceptBallotNumPair,messageParts[5]);
				}
				else {
					MessageCommunication.sendDecideUnicast(this.replica.replicaId,senderReplicaId, paxosId, currentPaxos.logEnrties);
				}
			}
			else {
				//already decided
				currentPaxos = this.replica.paxosEntries.get(numEntries - 1);
				MessageCommunication.sendDecideUnicast(this.replica.replicaId,senderReplicaId, paxosId, currentPaxos.logEnrties);
			}
			break;
		case 5:
			//onreceiveDecide
			String value = messageParts[3];
			ArrayList<String> Parts = new ArrayList<String>();
	    	String [] tempArray = value.split(":");
	    	for(String x: tempArray) {
	    		Parts.add(x);
	    	}
			if(paxosId < numEntries - 1) {
				//Redundant check
			    	currentPaxos = this.replica.paxosEntries.get(paxosId);
			    	currentPaxos.onreceiveDecide(Parts);
			}
			else if( paxosId == numEntries - 1) {
				//check if it is active or not
				currentPaxos = this.replica.paxosEntries.get(numEntries - 1);
				//active and join it - else already decided and send decide
				/**
				if(!currentPaxos.isDecided) {
					currentPaxos.onreceiveDecide(value);
				}
				*/
				currentPaxos.onreceiveDecide(Parts);
			}
			else {
				//future stuff in which I did not participate because I was down
				this.replica.logger.write("RECVD DECIDE MSG FOR PAXOS INSTANCE " + String.valueOf(paxosId) + " BUT IGNORED BECAUSE THIS REPLICA WAS DOWN IN INITIAL PHASES.");
			}
			break;
		case 6:
			String value1 = messageParts[3];
			ArrayList<String> Parts1 = new ArrayList<String>();
	    	String [] tempArray1 = value1.split(":");
	    	for(String x: tempArray1) {
	    		Parts1.add(x);
	    	}
			if(paxosId < numEntries - 1) {
				//Redundant check
			    	currentPaxos = this.replica.paxosEntries.get(paxosId);
			    	currentPaxos.onreceiveDecide(Parts1);
			}
			else if( paxosId == numEntries - 1) {
				//check if it is active or not
				currentPaxos = this.replica.paxosEntries.get(numEntries - 1);
				//active and join it - else already decided and send decide
					currentPaxos.onreceiveDecide(Parts1);
			}
			else {
				//future stuff in which I did not participate because I was down
				this.replica.logger.write("RECVD DECIDE MSG FOR PAXOS INSTANCE " + String.valueOf(paxosId) + " BUT IGNORED BECAUSE THIS REPLICA WAS DOWN IN INITIAL PHASES.");
			}
			break;
		case 7:

			//received recoverme message from a failed node
			int highestindex = paxosId;
			int recoverNodeId = senderReplicaId;
			int num = 0;
			if(numEntries >= 1) {
			if(this.replica.paxosEntries.get(numEntries -1).isDecided) {
				num = numEntries - 1;
			}
			else {
				num = numEntries - 2;
			}
			if(highestindex <= num ) {
				//sendUpdate or everythinguptodate
				ArrayList<String> newValues = new ArrayList<String>();
				
				for(int i=highestindex +1;i<=num;i++) {
					StringBuilder message = new StringBuilder();
					ArrayList<String> entry = this.replica.paxosEntries.get(i).logEnrties;
					for(String x : entry) {
						message.append(x+":");
					}
					newValues.add(message.subSequence(0, message.length()-1).toString());
					//newValues.add(this.replica.paxosEntries.get(i).valueWritten);
				}
				MessageCommunication.replyOnRecover(this.replica.replicaId,recoverNodeId,newValues);
			}
			else 
			{
				this.replica.logger.write("RECOVERING NODE KNOWS MUCH MORE THAN ME :(");
			}
			}
			break;
		case 8 :
			//receiving replyToRecover message
			
			// handle recovery message from other replicas and set isRecovered = true
			if(!this.replica.isRecovered && senderReplicaId != replica.replicaId) {
				int messageSize = paxosId;
				int numberOfEntries = replica.paxosEntries.size()-1;
				int highestDecidedIndex = 0 ;
				if(numberOfEntries >= 0) {
				if(replica.paxosEntries.get(numberOfEntries).isDecided)
				{
					highestDecidedIndex = numberOfEntries;
				}
				else
				{
					this.replica.logger.write("This would never happen.");
					replica.paxosEntries.remove(numberOfEntries);
					highestDecidedIndex = numberOfEntries-1;
				}
				}
				else
					highestDecidedIndex = -1;
				
				synchronized(this.replica.paxosEntries){
					System.out.println("Message Size = " + messageSize);
					for(int i = 0; i<messageSize;i++) {
						System.out.println("Message Size = " + messageSize + "Message" + messageParts[3+i]);
						String str = messageParts[3 + i];
						ArrayList<String> Parts2 = new ArrayList<String>();
				    	String [] tempArray2 = str.split(":");
				    	for(String x: tempArray2) {
				    		Parts2.add(x);
				    	}
						
						this.replica.paxosEntries.add(new Paxos(highestDecidedIndex+i+1,this.replica.replicaId,Parts2));
					}
				}
				replica.isRecovered = true;
			}
			//else don't do anything I am have recovered
			break;
		default:
			break;

		}
	}

}
