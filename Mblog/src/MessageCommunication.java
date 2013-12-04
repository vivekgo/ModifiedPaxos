import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MessageCommunication {


	// prepare message type = 1
	public static void sendPrepareMsg(int replicaId, int paxosId, BallotPair proposedBallotNumPair) {
		InterServerMessage message = new InterServerMessage();
		message.add("1");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(paxosId));
		message.add(Integer.toString(proposedBallotNumPair.processId));
		message.add(Integer.toString(proposedBallotNumPair.ballotNum));
		broadCast(message.getMessage());
	}

	// ack message type = 2
	public static void sendAckToPrepare(int replicaId, int proposerReplicaId, int paxosId, BallotPair ownBallotNumPair,
			BallotPair acceptedBallotNumPair, String acceptedValue) {
		InterServerMessage message = new InterServerMessage();
		message.add("2");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(paxosId));
		message.add(Integer.toString(ownBallotNumPair.processId));
		message.add(Integer.toString(ownBallotNumPair.ballotNum));
		message.add(Integer.toString(acceptedBallotNumPair.processId));
		message.add(Integer.toString(acceptedBallotNumPair.ballotNum));
		message.add(acceptedValue);
		unicastToServer(message.getMessage(),proposerReplicaId);
	}

	// nAck message type = 3
	public static void sendNAckToPrepare (int replicaId, int proposerReplicaId, int paxosId, BallotPair proposedBallotNumPair) {
		InterServerMessage message = new InterServerMessage();
		message.add("3");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(paxosId));
		message.add(Integer.toString(proposedBallotNumPair.processId));
		message.add(Integer.toString(proposedBallotNumPair.ballotNum));
		unicastToServer(message.getMessage(),proposerReplicaId);
	}

	// accept message type = 4
	public static void sendAccept(int replicaId, int paxosId, BallotPair proposeBallotNumPair,
			String ValueToWrite) {
		InterServerMessage message = new InterServerMessage();
		message.add("4");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(paxosId));
		message.add(Integer.toString(proposeBallotNumPair.processId));
		message.add(Integer.toString(proposeBallotNumPair.ballotNum));
		message.add(ValueToWrite);
		broadCast(message.getMessage());

	}
	
	// Decide message type = 5
	public static void sendDecide(int replicaId, int paxosId, String acceptedValue) {
		InterServerMessage message = new InterServerMessage();
		message.add("5");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(paxosId));
		message.add(acceptedValue);
		broadCast(message.getMessage());
	}
	// Decide message type = 6

	public static void sendDecideUnicast(int replicaId, int proposerReplicaId,
			int paxosId, ArrayList<String> ArrayValues) {
		InterServerMessage message = new InterServerMessage();
		message.add("6");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(paxosId));
		StringBuilder Values = new StringBuilder();
		for (String x : ArrayValues)
		{
			Values.append(x+":");
		}
		message.add(Values.subSequence(0, Values.length()-1).toString());
		unicastToServer(message.getMessage(),proposerReplicaId);
		
	}

	
	public static void sendRecover(int replicaId,int highestDecidedIndex) {
		InterServerMessage message = new InterServerMessage();
		message.add("7");
		message.add(Integer.toString(replicaId));
		message.add(Integer.toString(highestDecidedIndex));
		broadCast(message.getMessage());
	}
	
	public static void replyOnRecover (int replicaId, int proposerId, ArrayList<String> messages)
	{
		InterServerMessage message = new InterServerMessage();
		message.add("8");
		message.add(Integer.toString(replicaId));
		message.add (Integer.toString(messages.size()));
		for(int i = 0; i< messages.size();i++)
		{
			message.add(messages.get(i));
		}
		unicastToServer( message.getMessage(),proposerId);
	}

	private static void broadCast(String message) {
		for (ReplicaCommInfo receiver : Replica.replicas) {
			Socket clientSocket;
			try {
				// if (receiver.replicaId != replicaId) {
				clientSocket = new Socket(receiver.replicaIP, receiver.serverSocketId);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.writeBytes(message + "\n");
				clientSocket.close();
				// }

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void unicastToServer(String replyMessage, int replicaId) {
		try {
			
			ReplicaCommInfo communicationInfo = Replica.replicas.get(replicaId);
			Socket clientSocket = new Socket(communicationInfo.replicaIP, communicationInfo.serverSocketId);
	        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	        outToServer.writeBytes(replyMessage + "\n");
	        clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




}
