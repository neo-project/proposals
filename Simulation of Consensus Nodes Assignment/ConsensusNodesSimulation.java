import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ConsensusNodesSimulation {

	// Configuration
	static float neoConsortiumHoldings = 0.4f; // NEO holdings
	static float economicConsortiumHoldings = 0.2f; // Economic holdings
	static float totalVotingShares = neoConsortiumHoldings + economicConsortiumHoldings;
	static int numberOfConsensusNodes = 0;
	static int percentaceMultiplier = 10000;
	static int weightLowIndex = (int) (0.25 * percentageMultiplier);
	static int weightHighIndex = (int) (0.75 * percentageMultiplier);

	static File saveFile = new File("C:\\Users\\<<user>>\\Desktop\\simulation_N40_E20.csv");
	static FileWriter fw;

	public static void main(String[] args) {
		
		//Set up csv document
		try {
			fw = new FileWriter(saveFile, true);
			fw.write("Economic power;NEO nominee split;Economic nominee split;Total consensus nodes");
			fw.write(System.getProperty( "line.separator" ));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		// neoNominees = number of nominees NEO consortium split into
		// economicNominees = number of nominees economic consortium split into
		// neoSplit = number of nominees NEO consortium vote for
		// economicSplit  = number of nominees economic consortium vote for
		
		ArrayList<Nominee> conensusNodes;
		for (int neoNominees = 7; neoNominees < 31; neoNominees++) {
			for (int economicNominees = 7; economicNominees < 301; economicNominees++) {
				conensusNodes = calculateVoteResult(neoNominees, economicNominees);
				writeVoteResult(conensusNodes, neoNominees, economicNominees);
			}
		}
	}

	/** 
	 * Neo consortium and economic consortium spread their votes equally among their own nominees
	 * 
	 * @param neoNominees = number of nominees NEO consortium split into
	 * @param economicNominees = number of nominees economic consortium split into
	 * @return Consensus nodes sorted by votes
	 */
	private static ArrayList<Nominee> calculateVoteResult(int neoNominees, int economicNominees) {
		// Set number of votes for each nominee and count votes in ballot
		ArrayList<Nominee> voteResult = new ArrayList<>();
		float[] votesInBallot = new float[percentageMultiplier];
		int arrayIndex = 0;
		for (int i = 0; i < neoNominees; i++) {
			voteResult.add(new Nominee(true, neoConsortiumHoldings / totalVotingShares, false));
			// Split per percentage and put in array to be sorted
			int percentage = (int)(percentageMultiplier * ( (neoConsortiumHoldings /  (float) neoNominees) /  totalVotingShares ));
			for (int j = 0; j < percentage; j++){
				votesInBallot[arrayIndex] = neoNominees;
				arrayIndex++;
			}
		}
		for (int i = 0; i < economicNominees; i++) {
			voteResult.add(new Nominee(false, economicConsortiumHoldings/ totalVotingShares, false));
			// Split per percentage and put in array to be sorted
			int percentage = (int)(percentageMultiplier * ( (economicConsortiumHoldings / (float) economicNominees) /  totalVotingShares ));
			for (int j = 0; j < percentage; j++){
				votesInBallot[arrayIndex] = economicNominees;
				arrayIndex++;
			}
		}
		java.util.Arrays.sort(votesInBallot);

		// Calculate number of total consensus nodes by average vote in ballots for mid-50%
		float helpVariableForTotalVotes = 0;
		for (int i = weightLowIndex; i < weightHighIndex; i++) {
			helpVariableForTotalVotes += votesInBallot[i];
		}
		numberOfConsensusNodes = (int) (Math.floor((helpVariableForTotalVotes / (weightHighIndex - weightLowIndex))));
		
		// Handle edge case for rounding error
		if (numberOfConsensusNodes < 2) numberOfConsensusNodes = 2;

		// Sort nominees by votes
		Collections.sort(voteResult, new Comparator<Nominee>() {
			@Override public int compare(Nominee c1, Nominee c2) {
				return ( Double.compare(c2.getVotes(),(c1.getVotes()))); // Descending
			}
		}); 

		// Assign consensus nodes
		ArrayList<Nominee> consensusNodes = new ArrayList<>();
		for (int i = 0; i < numberOfConsensusNodes; i ++) {
			if (i < voteResult.size()) {
				consensusNodes.add(voteResult.get(i));
			} else {
				// Add system bookkeeping node
				consensusNodes.add(new Nominee(true, 0f, true));
			}
		}

		return consensusNodes;
	}


	/**
	 * @param consensusNodes
	 * @param neoNominees
	 * @param economicNominees
	 */
	private static void writeVoteResult(ArrayList<Nominee> consensusNodes, int neoNominees, int economicNominees) {

		try {
			fw = new FileWriter(saveFile, true);

			int numberOfNeoNodes = 0;
			int numberOfEconomicNodes = 0;
			for (int i = 0; i < consensusNodes.size(); i++) {
				if (consensusNodes.get(i).isNEO()) {
					numberOfNeoNodes++;
				} else {
					numberOfEconomicNodes++;
				}
			}

			float economicPower = (float) numberOfEconomicNodes / (numberOfNeoNodes + numberOfEconomicNodes);
			fw.write("" + economicPower + ";" + neoNominees + ";" + economicNominees + ";" + consensusNodes.size());
			fw.write(System.getProperty( "line.separator" ));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
