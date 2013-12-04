
public class AcceptedBallotNumAndValue {
	BallotPair r_acceptedBallotNumPair;
	String r_acceptedValue;
	int numTimes;
	
	public AcceptedBallotNumAndValue(BallotPair b, String s) {
		this.r_acceptedBallotNumPair = b;
		this.r_acceptedValue = s;
		this.numTimes = 0;
	}
	public boolean equalTo(AcceptedBallotNumAndValue arg0) {
		if(this.r_acceptedBallotNumPair.compareTo(arg0.r_acceptedBallotNumPair) == 0 && (this.r_acceptedValue).equals(arg0.r_acceptedValue)) {
			return true;
		}
		else
			return false;
	}

}
