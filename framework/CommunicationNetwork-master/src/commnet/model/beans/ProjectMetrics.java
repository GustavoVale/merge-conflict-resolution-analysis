package commnet.model.beans;

public class ProjectMetrics {

	private Integer IdDB;
	private Integer projectIdDB;

	private Integer loc;
	private Integer msComputed;
	private Integer msIgnored;
	private Integer msConflicted;
	private Integer numberOfFiles;
	private Integer numberOfChunks;
	private Integer numberOfCommits;
	private Integer numberOfDevelopers;
	private Integer numberOfDistinctFiles;

	public ProjectMetrics(Integer projectIdDB) {
		this(null, projectIdDB, null, null, null, null, null, null, null, null, null);
	}
	
	public ProjectMetrics(Integer idDB, Integer projectIdDB) {
		this(idDB, projectIdDB, null, null, null, null, null, null, null, null, null);
	}

	public ProjectMetrics(Integer idDB, Integer projectIdDB, Integer loc, Integer msComputed, Integer msIgnored,
			Integer msConflicted, Integer numFiles, Integer numChunks, Integer numCommits, Integer numDev,
			Integer numDistictFiles) {

		setIdDB(idDB);
		setProjectIdDB(projectIdDB);
		setLoc(loc);
		setMsComputed(msComputed);
		setMsIgnored(msIgnored);
		setMsConflicted(msConflicted);
		setNumberOfFiles(numFiles);
		setNumberOfChunks(numChunks);
		setNumberOfCommits(numCommits);
		setNumberOfDevelopers(numDev);
		setNumberOfDistinctFiles(numDistictFiles);
	}

	public Integer getIdDB() {
		return IdDB;
	}

	public void setIdDB(Integer idDB) {
		IdDB = idDB;
	}

	public Integer getProjectIdDB() {
		return projectIdDB;
	}

	public void setProjectIdDB(Integer projectIdDB) {
		this.projectIdDB = projectIdDB;
	}

	public Integer getLoc() {
		return loc;
	}

	public void setLoc(Integer loc) {
		this.loc = loc;
	}

	public Integer getMsComputed() {
		return msComputed;
	}

	public void setMsComputed(Integer msComputed) {
		this.msComputed = msComputed;
	}

	public Integer getMsIgnored() {
		return msIgnored;
	}

	public void setMsIgnored(Integer msIgnored) {
		this.msIgnored = msIgnored;
	}

	public Integer getMsConflicted() {
		return msConflicted;
	}

	public void setMsConflicted(Integer msConflicted) {
		this.msConflicted = msConflicted;
	}

	public Integer getNumberOfFiles() {
		return numberOfFiles;
	}

	public void setNumberOfFiles(Integer numberOfFiles) {
		this.numberOfFiles = numberOfFiles;
	}

	public Integer getNumberOfChunks() {
		return numberOfChunks;
	}

	public void setNumberOfChunks(Integer numberOfChunks) {
		this.numberOfChunks = numberOfChunks;
	}

	public Integer getNumberOfCommits() {
		return numberOfCommits;
	}

	public void setNumberOfCommits(Integer numberOfCommits) {
		this.numberOfCommits = numberOfCommits;
	}

	public Integer getNumberOfDevelopers() {
		return numberOfDevelopers;
	}

	public void setNumberOfDevelopers(Integer numberOfDevelopers) {
		this.numberOfDevelopers = numberOfDevelopers;
	}

	public Integer getNumberOfDistinctFiles() {
		return numberOfDistinctFiles;
	}

	public void setNumberOfDistinctFiles(Integer numberOfDistinctFiles) {
		this.numberOfDistinctFiles = numberOfDistinctFiles;
	}

}
