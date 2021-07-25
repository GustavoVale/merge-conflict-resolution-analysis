package commnet.model.beans;

import java.sql.Timestamp;

public class Developer {

	private Integer idDB;
	private Integer mergeScenarioIdDB;
	private Integer contributorIdDB;

	private Integer numberOfFilesChangedLeft;
	private Integer numberOfFilesChangedRight;
	private Integer numberOfChunksChangedLeft;
	private Integer numberOfChunksChangedRight;
	private Integer numberOfLinesChangedLeft;
	private Integer numberOfLinesChangedRight;
	private Integer numberOfCommitsLeft;
	private Integer numberOfCommitsRight;
	private boolean lastChangeLeft;
	private boolean lastChangeRight;
	private boolean isIntegrator;
	private boolean contributeToConflict;
	private boolean isCoreDeveloper;
	private boolean isLeftBranchLeader;
	private boolean isRightBranchLeader;

	private Timestamp lastCommitLeftDate;
	private Timestamp lastCommitRightDate;

	public Developer(Integer idDB, Integer msIdDB, Integer contIdDB) {
		this(idDB, msIdDB, contIdDB, null, null, null, null, null, null, null, null, false, false, false, false, false,
				false, false);
	}

	public Developer(Integer idDB, Integer msIdDB, Integer contIdDB, boolean isCore) {
		this(idDB, msIdDB, contIdDB, null, null, null, null, null, null, null, null, false, false, false, false, isCore,
				false, false);
	}

	public Developer(Integer idDB, Integer msIdDB, Integer contIdDB, Integer nfcl, Integer nchcl, Integer nlcl,
			Integer ncl, Integer nfcr, Integer nchcr, Integer nlcr, Integer ncr, boolean lcLeft, boolean lcRight,
			boolean isIntegrator, boolean contributeToConflict, boolean isCoreDev, boolean isLeftLeader,
			boolean isRightLeader) {
		setIdDB(idDB);
		setMergeScenarioIdDB(msIdDB);
		setContributorIdDB(contIdDB);
		setNumberOfFilesChangedLeft(nfcl);
		setNumberOfFilesChangedRight(nfcr);
		setNumberOfChunksChangedLeft(nchcl);
		setNumberOfChunksChangedRight(nchcr);
		setNumberOfLinesChangedLeft(nlcl);
		setNumberOfLinesChangedRight(nlcr);
		setNumberOfCommitsLeft(ncl);
		setNumberOfCommitsRight(ncr);
		setLastChangeLeft(lcLeft);
		setLastChangeRight(lcRight);
		setIntegrator(isIntegrator);
		setContributeToConflict(contributeToConflict);
		setCoreDeveloper(isCoreDev);
		setLeftBranchLeader(isLeftLeader);
		setRightBranchLeader(isRightLeader);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getMergeScenarioIdDB() {
		return mergeScenarioIdDB;
	}

	public void setMergeScenarioIdDB(Integer mergeScenarioIdDB) {
		this.mergeScenarioIdDB = mergeScenarioIdDB;
	}

	public Integer getContributorIdDB() {
		return contributorIdDB;
	}

	public void setContributorIdDB(Integer contributorIdDB) {
		this.contributorIdDB = contributorIdDB;
	}

	public Integer getNumberOfFilesChangedLeft() {
		return numberOfFilesChangedLeft;
	}

	public void setNumberOfFilesChangedLeft(Integer numberOfFilesChanged) {
		this.numberOfFilesChangedLeft = numberOfFilesChanged;
	}

	public Integer getNumberOfChunksChangedLeft() {
		return numberOfChunksChangedLeft;
	}

	public void setNumberOfChunksChangedLeft(Integer numberOfChunksChanged) {
		this.numberOfChunksChangedLeft = numberOfChunksChanged;
	}

	public Integer getNumberOfLinesChangedLeft() {
		return numberOfLinesChangedLeft;
	}

	public void setNumberOfLinesChangedLeft(Integer numberOfLinesChanged) {
		this.numberOfLinesChangedLeft = numberOfLinesChanged;
	}

	public Integer getNumberOfCommitsLeft() {
		return numberOfCommitsLeft;
	}

	public void setNumberOfCommitsLeft(Integer numberOfCommits) {
		this.numberOfCommitsLeft = numberOfCommits;
	}

	public Integer getNumberOfFilesChangedRight() {
		return numberOfFilesChangedRight;
	}

	public void setNumberOfFilesChangedRight(Integer numberOfFilesChanged) {
		this.numberOfFilesChangedRight = numberOfFilesChanged;
	}

	public Integer getNumberOfChunksChangedRight() {
		return numberOfChunksChangedRight;
	}

	public void setNumberOfChunksChangedRight(Integer numberOfChunksChanged) {
		this.numberOfChunksChangedRight = numberOfChunksChanged;
	}

	public Integer getNumberOfLinesChangedRight() {
		return numberOfLinesChangedRight;
	}

	public void setNumberOfLinesChangedRight(Integer numberOfLinesChanged) {
		this.numberOfLinesChangedRight = numberOfLinesChanged;
	}

	public Integer getNumberOfCommitsRight() {
		return numberOfCommitsRight;
	}

	public void setNumberOfCommitsRight(Integer numberOfCommits) {
		this.numberOfCommitsRight = numberOfCommits;
	}

	public boolean isLastChangeLeft() {
		return lastChangeLeft;
	}

	public void setLastChangeLeft(boolean lastChangeLeft) {
		this.lastChangeLeft = lastChangeLeft;
	}

	public boolean isLastChangeRight() {
		return lastChangeRight;
	}

	public void setLastChangeRight(boolean lastChangeRight) {
		this.lastChangeRight = lastChangeRight;
	}

	public boolean isIntegrator() {
		return isIntegrator;
	}

	public void setIntegrator(boolean isIntegrator) {
		this.isIntegrator = isIntegrator;
	}

	public boolean isContributeToConflict() {
		return contributeToConflict;
	}

	public void setContributeToConflict(boolean contributeToConflict) {
		this.contributeToConflict = contributeToConflict;
	}

	public boolean isCoreDeveloper() {
		return isCoreDeveloper;
	}

	public void setCoreDeveloper(boolean isCoreDeveloper) {
		this.isCoreDeveloper = isCoreDeveloper;
	}

	public boolean isLeftBranchLeader() {
		return isLeftBranchLeader;
	}

	public void setLeftBranchLeader(boolean isLeftBranchLeader) {
		this.isLeftBranchLeader = isLeftBranchLeader;
	}

	public boolean isRightBranchLeader() {
		return isRightBranchLeader;
	}

	public void setRightBranchLeader(boolean isRightBranchLeader) {
		this.isRightBranchLeader = isRightBranchLeader;
	}

	public Timestamp getLastCommitLeftDate() {
		return lastCommitLeftDate;
	}

	public void setLastCommitLeftDate(Timestamp lastCommitLeftDate) {
		this.lastCommitLeftDate = lastCommitLeftDate;
	}

	public Timestamp getLastCommitRightDate() {
		return lastCommitRightDate;
	}

	public void setLastCommitRightDate(Timestamp lastCommitRightDate) {
		this.lastCommitRightDate = lastCommitRightDate;
	}

}
