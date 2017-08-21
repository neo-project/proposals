public class Nominee {
	
	private boolean NEO = false;
	private boolean systemNode = false;
	private float votes = 0;
	
	/**
	 * 
	 * @param isNEO
	 * @param votes
	 * @param isSystemNode
	 */
	public Nominee(boolean isNEO, float votes, boolean isSystemNode) {
		setNEO(isNEO);
		setVotes(votes);
		setSystemNode(isSystemNode);
	}
	
	public boolean isNEO() {
		return NEO;
	}
	public void setNEO(boolean nEO) {
		NEO = nEO;
	}
	public float getVotes() {
		return votes;
	}
	public void setVotes(float votes) {
		this.votes = votes;
	}

	public boolean isSystemNode() {
		return systemNode;
	}

	public void setSystemNode(boolean systemNode) {
		this.systemNode = systemNode;
	}



}
