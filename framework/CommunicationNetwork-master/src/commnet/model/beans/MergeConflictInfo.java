package commnet.model.beans;

import java.util.ArrayList;
import java.util.List;

import gitwrapper.repo.Commit;

public class MergeConflictInfo {

	private Integer idDB;
	private Integer chunkIdDB;
	private Integer leftCommitIdDB;
	private Integer rightCommitIdDB;
	private String leftCommitCode;
	private String rightCommitCode;
	private String mergedCode;
	private long leftMergeTimeDifference;
	private long rightMergeTimeDifference;

	private List<Commit> mergeScenarioCommits = new ArrayList<>();

	public MergeConflictInfo(Integer IdDB) {
		this(IdDB, null, null, null, null, null, null, 0, 0, null);
	}

	public MergeConflictInfo(Integer chunkID, Integer leftCommitIdDB, Integer rightCommitIdDB, String leftCommitCode,
			String rightCommitCode, String mergedCode, long leftMergeTimeDifference, long rightMergeTimeDifference,
			List<Commit> commitList) {
		this(null, chunkID, leftCommitIdDB, rightCommitIdDB, leftCommitCode, rightCommitCode, mergedCode,
				leftMergeTimeDifference, rightMergeTimeDifference, commitList);
	}

	public MergeConflictInfo(Integer idDb, Integer chunkID, Integer leftCommitIdDB, Integer rightCommitIdDB,
			String leftCommitCode, String rightCommitCode, String mergedCode, long leftMergeTimeDifference,
			long rightMergeTimeDifference) {
		this(idDb, chunkID, leftCommitIdDB, rightCommitIdDB, leftCommitCode, rightCommitCode, mergedCode,
				leftMergeTimeDifference, rightMergeTimeDifference, null);
	}

	public MergeConflictInfo(Integer idDb, Integer chunkID, Integer leftCommitIdDB, Integer rightCommitIdDB,
			String leftCommitCode, String rightCommitCode, String mergedCode, long leftMergeTimeDifference,
			long rightMergeTimeDifference, List<Commit> commitList) {
		this.idDB = idDb;
		this.chunkIdDB = chunkID;
		this.leftCommitIdDB = leftCommitIdDB;
		this.rightCommitIdDB = rightCommitIdDB;
		this.leftCommitCode = leftCommitCode;
		this.rightCommitCode = rightCommitCode;
		this.mergedCode = mergedCode;
		this.leftMergeTimeDifference = leftMergeTimeDifference;
		this.rightMergeTimeDifference = rightMergeTimeDifference;

		if (commitList != null) {
			this.mergeScenarioCommits = commitList;
		}
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getChunkIdDB() {
		return chunkIdDB;
	}

	public void setChunkIdDB(Integer chunkIdDB) {
		this.chunkIdDB = chunkIdDB;
	}

	public String getLeftCommitCode() {
		return leftCommitCode;
	}

	public void setLeftCommitCode(String leftCommitCode) {
		this.leftCommitCode = leftCommitCode;
	}

	public String getRightCommitCode() {
		return rightCommitCode;
	}

	public void setRightCommitCode(String rightCommitCode) {
		this.rightCommitCode = rightCommitCode;
	}

	public String getMergedCode() {
		return mergedCode;
	}

	public void setMergedCode(String mergedCode) {
		this.mergedCode = mergedCode;
	}

	public long getLeftMergeTimeDifference() {
		return leftMergeTimeDifference;
	}

	public void setLeftMergeTimeDifference(long leftMergeTimeDifference) {
		this.leftMergeTimeDifference = leftMergeTimeDifference;
	}

	public long getRightMergeTimeDifference() {
		return rightMergeTimeDifference;
	}

	public void setRightMergeTimeDifference(long rightMergeTimeDifference) {
		this.rightMergeTimeDifference = rightMergeTimeDifference;
	}

	public Integer getLeftCommitIdDB() {
		return leftCommitIdDB;
	}

	public void setLeftCommitIdDB(Integer leftCommitID) {
		this.leftCommitIdDB = leftCommitID;
	}

	public Integer getRightCommitIdDB() {
		return rightCommitIdDB;
	}

	public void setRightCommitIdDB(Integer rightCommitID) {
		this.rightCommitIdDB = rightCommitID;
	}

	public List<Commit> getMergeScenarioCommits() {
		return mergeScenarioCommits;
	}

	public void setMergeScenarioCommits(List<Commit> mergeScenarioCommits) {
		this.mergeScenarioCommits = mergeScenarioCommits;
	}

}
