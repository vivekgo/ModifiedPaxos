
public class BallotPair implements Comparable<BallotPair>{
	int ballotNum;
	int processId;
	
	
	public BallotPair(int ballotNum, int processId)
	{
		this.ballotNum = ballotNum;
		this.processId = processId;
	}
	public BallotPair()
	{
		this.ballotNum = 0;
		this.processId = 0;
	}
	@Override
	public int compareTo(BallotPair arg0) {
		if(this.ballotNum != arg0.ballotNum){
			return this.ballotNum - arg0.ballotNum;
		}
		else
		{
			return this.processId - arg0.processId;

		}
	}
	
	public String toString()
	{
		return "(" + Integer.toString(ballotNum) + "," + Integer.toString(processId) + ")" ;  
	}
}
