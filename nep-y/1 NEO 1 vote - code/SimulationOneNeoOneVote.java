package governance;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class SimulationOneNeoOneVote {

	// Configuration
	static int totalNEO = 100000000;
	static float neoConsortiumHoldings = 0.40f; // NEO holdings
	static float economicConsortiumHoldings = 0.15f; // Economic holdings
	static float totalVotingShares = neoConsortiumHoldings + economicConsortiumHoldings;
	static int percentageMultiplier = 1000000;
	static int weightLowIndex = (int) (0.25 * percentageMultiplier);
	static int weightHighIndex = (int) (0.75 * percentageMultiplier);
	static ArrayList<Nominee> voteResult = new ArrayList<>();

	static File saveFile = new File("C:\\Users\\malco\\Desktop\\simulation_N40_E15.csv");
	static FileWriter fw;

	public static void main(String[] args) {
		
		//Set up csv document
		try {
			fw = new FileWriter(saveFile, true);
			fw.write("Economic power;NEO nominee split;Economic nominee split;Total consensus nodes; Economic Node NEO Support");
			fw.write(System.getProperty( "line.separator" ));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		// neoNominees = number of nominees NEO consortium split into
		// economicNominees = number of nominees economic consortium split into
		
		ArrayList<Nominee> conensusNodes;
		for (int neoNominees = 1; neoNominees < 31; neoNominees++) {
			for (int economicNominees = 1; economicNominees < 301; economicNominees++) {
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
	static ArrayList<Nominee> calculateVoteResult(int neoNominees, int economicNominees) {
		// Set number of votes for each nominee and count votes in ballot
		float[] votesInBallot = vote(neoNominees, economicNominees);

		// Calculate number of total consensus nodes by average vote in ballots for mid-50%
		int numberOfConsensusNodes = calculateConsensusNodes(votesInBallot);
		
		// Handle minimum consensus nodes
		if (numberOfConsensusNodes < 7) numberOfConsensusNodes = 7;

		// Sort nominees by votes
		Collections.sort(voteResult, new Comparator<Nominee>() {
			@Override public int compare(Nominee c1, Nominee c2) {
				return ( Double.compare(c2.getVotes(),(c1.getVotes()))); // Descending
			}
		}); 

		// Assign consensus nodes
		ArrayList<Nominee> consensusNodes = assignConsensusNodes(voteResult, numberOfConsensusNodes);

		return consensusNodes;
	}

	private static ArrayList<Nominee> assignConsensusNodes(ArrayList<Nominee> voteResult, int numberOfConsensusNodes) {
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

	static int calculateConsensusNodes(float[] votesInBallot) {
		float helpVariableForTotalVotes = 0;
		for (int i = weightLowIndex; i < weightHighIndex; i++) {
			helpVariableForTotalVotes += votesInBallot[i];
		}
		int numberOfConsensusNodes = (int) (Math.floor((helpVariableForTotalVotes / (weightHighIndex - weightLowIndex))));
		return numberOfConsensusNodes;
	}

	static float[] vote(int neoNominees, int economicNominees) {
		float[] votesInBallot = new float[percentageMultiplier];
		int arrayIndex = 0;
		voteResult = new ArrayList<>();
		for (int i = 0; i < neoNominees; i++) {
			voteResult.add(new Nominee(true, (neoConsortiumHoldings /  (float) neoNominees) /  totalVotingShares, false));
			// Split per percentage and put in array to be sorted
			int percentage = (int)(percentageMultiplier * ( (neoConsortiumHoldings /  (float) neoNominees) /  totalVotingShares ));
			for (int j = 0; j < percentage; j++){
				votesInBallot[arrayIndex] = neoNominees;
				arrayIndex++;
			}
		}
		for (int i = 0; i < economicNominees; i++) {
			voteResult.add(new Nominee(false, (economicConsortiumHoldings / (float) economicNominees) /  totalVotingShares, false));
			// Split per percentage and put in array to be sorted
			int percentage = (int)(percentageMultiplier * ( (economicConsortiumHoldings / (float) economicNominees) /  totalVotingShares ));
			for (int j = 0; j < percentage; j++){
				votesInBallot[arrayIndex] = economicNominees;
				arrayIndex++;
			}
		}
		java.util.Arrays.sort(votesInBallot);
		return votesInBallot;
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
			float economicNodeNeoSupport = (economicConsortiumHoldings * totalNEO) / economicNominees;
			fw.write("" + economicPower + ";" + neoNominees + ";" + economicNominees + ";" + consensusNodes.size() + ";" + economicNodeNeoSupport);
			fw.write(System.getProperty( "line.separator" ));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
