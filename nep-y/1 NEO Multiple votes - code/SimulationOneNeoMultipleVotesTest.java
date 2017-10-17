package governance;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;


public class SimulationOneNeoMultipleVotesTest {

	@Test 
	public void	testVote() {

		Float neoShares = SimulationOneNeoMultipleVotes.neoConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		int m = SimulationOneNeoMultipleVotes.percentageMultiplier;
		float[] votesInBallot = SimulationOneNeoMultipleVotes.vote(20, 400);
		assertTrue(votesInBallot[200] == 20f);
		assertTrue(votesInBallot[m - 200] == 400f);
		assertTrue(votesInBallot[(int) (neoShares * m) - 200] == 20f);

	}

	@Test 
	public void	testCalculateConsensusNodes1() {
		Float neoShares = SimulationOneNeoMultipleVotes.neoConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		Float evilShares = SimulationOneNeoMultipleVotes.economicConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		neoShares = neoShares - 0.25f;
		evilShares = evilShares - 0.25f;

		float totalConsensusNodes = (2 * neoShares / 0.5f) + (4  * evilShares / 0.5f);
		float[] votesInBallot = SimulationOneNeoMultipleVotes.vote(2, 4);
		assertTrue(SimulationOneNeoMultipleVotes.calculateConsensusNodes(votesInBallot) == Math.floor(totalConsensusNodes));
	}

	@Test 
	public void	testCalculateConsensusNodes2() {
		Float neoShares = SimulationOneNeoMultipleVotes.neoConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		Float evilShares = SimulationOneNeoMultipleVotes.economicConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		neoShares = neoShares - 0.25f;
		evilShares = evilShares - 0.25f;

		float totalConsensusNodes = (20 * neoShares / 0.5f) + (400  * evilShares / 0.5f);
		float[] votesInBallot = SimulationOneNeoMultipleVotes.vote(20, 400);
		assertTrue(SimulationOneNeoMultipleVotes.calculateConsensusNodes(votesInBallot) == Math.floor(totalConsensusNodes));
	}

	@Test 
	public void	testCalculateVoteResult1() {
		int neoNominees = 2;
		int evilNominees = 4;
		int totalNominees = neoNominees + evilNominees;

		Float neoShares = SimulationOneNeoMultipleVotes.neoConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		Float evilShares = SimulationOneNeoMultipleVotes.economicConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		Float neoSharesCalc = neoShares - 0.25f;
		Float evilSharesCalc = evilShares - 0.25f;
		int totalConsensusNodes = (int) (Math.floor(neoNominees * neoSharesCalc / 0.5f) + (evilNominees  * evilSharesCalc / 0.5f));
		totalConsensusNodes = Math.max(totalConsensusNodes, 7);
		float neoNomineePower = SimulationOneNeoMultipleVotes.neoConsortiumHoldings;
		float evilNomineePower = SimulationOneNeoMultipleVotes.economicConsortiumHoldings;
		ArrayList<Nominee> consensusNodes1 = new ArrayList<Nominee>();
		int neoCount = 0;
		if (neoNomineePower > evilNomineePower) {
			for (int j = 0; j < neoNominees; j ++) {
				if (consensusNodes1.size() < totalNominees) {
					consensusNodes1.add(new Nominee(true, 0f, false));
					neoCount++;
				}
			}
			for (int j = 0; j < evilNominees; j ++) {
				if (consensusNodes1.size() < totalNominees)
					consensusNodes1.add(new Nominee(false, 0f, false));
			}
			for (int j = 0; j < totalNominees; j ++) {
				if (consensusNodes1.size() < totalNominees) {
					consensusNodes1.add(new Nominee(true, 0f, true));
					neoCount++;
				}
			}
		}
		float neoConsensusPower1 = neoCount / consensusNodes1.size();



		ArrayList<Nominee> consensusNodes2 = SimulationOneNeoMultipleVotes.calculateVoteResult(neoNominees, evilNominees);
		neoCount = 0;
		for (int i = 0; i < totalConsensusNodes; i++) {
			if (consensusNodes2.get(i).isNEO()) {
				neoCount++;
			}
		}
		float neoConsensusPower2 = neoCount / consensusNodes2.size();


		assertTrue(neoConsensusPower1 == neoConsensusPower2);
	}
	
	@Test 
	public void	testCalculateVoteResult2() {
		int neoNominees = 20;
		int evilNominees = 400;
		int totalNominees = neoNominees + evilNominees;

		Float neoShares = SimulationOneNeoMultipleVotes.neoConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		Float evilShares = SimulationOneNeoMultipleVotes.economicConsortiumHoldings / SimulationOneNeoMultipleVotes.totalVotingShares;
		Float neoSharesCalc = neoShares - 0.25f;
		Float evilSharesCalc = evilShares - 0.25f;
		int totalConsensusNodes = (int) (Math.floor(neoNominees * neoSharesCalc / 0.5f) + (evilNominees  * evilSharesCalc / 0.5f));
		totalConsensusNodes = Math.max(totalConsensusNodes, 7);
		float neoNomineePower = SimulationOneNeoMultipleVotes.neoConsortiumHoldings;
		float evilNomineePower = SimulationOneNeoMultipleVotes.economicConsortiumHoldings;
		ArrayList<Nominee> consensusNodes1 = new ArrayList<Nominee>();
		int neoCount = 0;
		if (neoNomineePower > evilNomineePower) {
			for (int j = 0; j < neoNominees; j ++) {
				if (consensusNodes1.size() < totalNominees) {
					consensusNodes1.add(new Nominee(true, 0f, false));
					neoCount++;
				}
			}
			for (int j = 0; j < evilNominees; j ++) {
				if (consensusNodes1.size() < totalNominees)
					consensusNodes1.add(new Nominee(false, 0f, false));
			}
			for (int j = 0; j < totalNominees; j ++) {
				if (consensusNodes1.size() < totalNominees) {
					consensusNodes1.add(new Nominee(true, 0f, true));
					neoCount++;
				}
			}
		}
		float neoConsensusPower1 = neoCount / consensusNodes1.size();



		ArrayList<Nominee> consensusNodes2 = SimulationOneNeoMultipleVotes.calculateVoteResult(neoNominees, evilNominees);
		neoCount = 0;
		for (int i = 0; i < totalConsensusNodes; i++) {
			if (consensusNodes2.get(i).isNEO()) {
				neoCount++;
			}
		}
		float neoConsensusPower2 = neoCount / consensusNodes2.size();


		assertTrue(neoConsensusPower1 == neoConsensusPower2);
	}

}
