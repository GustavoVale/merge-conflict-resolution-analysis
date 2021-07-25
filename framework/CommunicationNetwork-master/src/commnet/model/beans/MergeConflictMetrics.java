package commnet.model.beans;

import commnet.model.enums.ChangeType;

public class MergeConflictMetrics {

	private Integer idDB;
	private Integer mergeConflictInfoIdDB;

	private ChangeType changeType;

	private Integer loc;
	private Integer leftLoc;
	private Integer rightLoc;
	private Integer cyclomaticComplexity;
	private Integer leftCyclomaticComplexity;
	private Integer rightCyclomaticComplexity;

	private boolean devHasKnowledge;

	private Integer projectId;
	private Integer mergeScenarioIdDB;
	private Integer mergeCommitIdDB;
	private String filePath;

	private CommitN mergeCommit;

	public MergeConflictMetrics(Integer id, Integer mergeConflictInfo, ChangeType changeType, Integer loc,
			Integer leftLoc, Integer rightLoc, Integer complexity, Integer leftComplexity, Integer rightComplexity,
			boolean devKnowledge) {
		this(id, mergeConflictInfo, changeType, loc, leftLoc, rightLoc, complexity, leftComplexity, rightComplexity,
				devKnowledge, null, null, null, null, null);
	}

	public MergeConflictMetrics(Integer id, Integer mergeConflictInfo, ChangeType changeType, Integer loc,
			Integer leftLoc, Integer rightLoc, Integer complexity, Integer leftComplexity, Integer rightComplexity,
			boolean devKnowledge, Integer projectId, Integer msId, Integer commitId, String filepath,
			CommitN mergeCommit) {
		setIdDB(id);
		setMergeConflictInfoIdDB(mergeConflictInfo);
		setChangeType(changeType);
		setLoc(loc);
		setLeftLoc(leftLoc);
		setRightLoc(rightLoc);
		setCyclomaticComplexity(complexity);
		setLeftCyclomaticComplexity(leftComplexity);
		setRightCyclomaticComplexity(rightComplexity);
		setDevHasKnowledge(devKnowledge);
		setProjectId(projectId);
		setMergeScenarioIdDB(msId);
		setMergeCommitIdDB(commitId);
		setFilePath(filepath);
		setMergeCommit(mergeCommit);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getMergeConflictInfoIdDB() {
		return mergeConflictInfoIdDB;
	}

	public void setMergeConflictInfoIdDB(Integer mergeConflictInfoDB) {
		this.mergeConflictInfoIdDB = mergeConflictInfoDB;
	}

	public Integer getLoc() {
		return loc;
	}

	public void setLoc(Integer loc) {
		this.loc = loc;
	}

	public Integer getLeftLoc() {
		return leftLoc;
	}

	public void setLeftLoc(Integer leftLoc) {
		this.leftLoc = leftLoc;
	}

	public Integer getRightLoc() {
		return rightLoc;
	}

	public void setRightLoc(Integer rightLoc) {
		this.rightLoc = rightLoc;
	}

	public Integer getCyclomaticComplexity() {
		return cyclomaticComplexity;
	}

	public void setCyclomaticComplexity(Integer cyclomaticComplexity) {
		this.cyclomaticComplexity = cyclomaticComplexity;
	}

	public Integer getLeftCyclomaticComplexity() {
		return leftCyclomaticComplexity;
	}

	public void setLeftCyclomaticComplexity(Integer leftCyclomaticComplexity) {
		this.leftCyclomaticComplexity = leftCyclomaticComplexity;
	}

	public Integer getRightCyclomaticComplexity() {
		return rightCyclomaticComplexity;
	}

	public void setRightCyclomaticComplexity(Integer rightCyclomaticComplexity) {
		this.rightCyclomaticComplexity = rightCyclomaticComplexity;
	}

	public boolean isDevHasKnowledge() {
		return devHasKnowledge;
	}

	public void setDevHasKnowledge(boolean devHasKnowledge) {
		this.devHasKnowledge = devHasKnowledge;
	}

	public ChangeType getChangeType() {
		return changeType;
	}

	public void setChangeType(ChangeType changeType) {
		this.changeType = changeType;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public Integer getMergeScenarioIdDB() {
		return mergeScenarioIdDB;
	}

	public void setMergeScenarioIdDB(Integer mergeScenarioIdDB) {
		this.mergeScenarioIdDB = mergeScenarioIdDB;
	}

	public Integer getMergeCommitIdDB() {
		return mergeCommitIdDB;
	}

	public void setMergeCommitIdDB(Integer mergeCommitIdDB) {
		this.mergeCommitIdDB = mergeCommitIdDB;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public CommitN getMergeCommit() {
		return mergeCommit;
	}

	public void setMergeCommit(CommitN mergeCommit) {
		this.mergeCommit = mergeCommit;
	}

}
