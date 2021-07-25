package commnet.model.beans;

public class FileMetrics {

	private Integer IdDB;
	private Integer fileIdDB;

	private Integer numberChunks;
	private Integer numberConflictedChunks;
	private Integer numberLeftChunks;
	private Integer numberRightChunks;

	private Integer numberCommits;
	private Integer numberLeftCommits;
	private Integer numberRightCommits;

	private Integer numberDevelopers;
	private Integer numberLeftDevelopers;
	private Integer numberRightDevelopers;
	private Integer numberBothSideDevelopers;
	
	private Integer loc;

	public FileMetrics(Integer fileIdDB) {
		this(null, fileIdDB, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public FileMetrics(Integer idDB, Integer fileIdDB) {
		this(idDB, fileIdDB, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public FileMetrics(Integer idDB, Integer fileIdDB, Integer nch, Integer ncch, Integer nlch, Integer nrch, Integer nco,
			Integer nlco, Integer nrco, Integer ndev, Integer nldev, Integer nrdev, Integer nbdev, Integer loc) {
		setIdDB(idDB);
		setFileIdDB(fileIdDB);
		setNumberChunks(nch);
		setNumberLeftChunks(nlch);
		setNumberRightChunks(nrch);
		setNumberCommits(nco);
		setNumberLeftCommits(nlco);
		setNumberRightCommits(nrco);
		setNumberDevelopers(ndev);
		setNumberLeftDevelopers(nldev);
		setNumberRightDevelopers(nrdev);
		setNumberBothSideDevelopers(nbdev);
		setLoc(loc);
	}

	public Integer getIdDB() {
		return IdDB;
	}

	public void setIdDB(Integer idDB) {
		IdDB = idDB;
	}

	public Integer getFileIdDB() {
		return fileIdDB;
	}

	public void setFileIdDB(Integer fileIdDB) {
		this.fileIdDB = fileIdDB;
	}

	public Integer getNumberChunks() {
		return numberChunks;
	}

	public void setNumberChunks(Integer numberChunks) {
		this.numberChunks = numberChunks;
	}

	public Integer getNumberConflictedChunks() {
		return numberConflictedChunks;
	}

	public void setNumberConflictedChunks(Integer numberConflictedChunks) {
		this.numberConflictedChunks = numberConflictedChunks;
	}

	public Integer getNumberLeftChunks() {
		return numberLeftChunks;
	}

	public void setNumberLeftChunks(Integer numberLeftChunks) {
		this.numberLeftChunks = numberLeftChunks;
	}

	public Integer getNumberRightChunks() {
		return numberRightChunks;
	}

	public void setNumberRightChunks(Integer numberRightChunks) {
		this.numberRightChunks = numberRightChunks;
	}

	public Integer getNumberCommits() {
		return numberCommits;
	}

	public void setNumberCommits(Integer numberCommits) {
		this.numberCommits = numberCommits;
	}

	public Integer getNumberLeftCommits() {
		return numberLeftCommits;
	}

	public void setNumberLeftCommits(Integer numberLeftCommits) {
		this.numberLeftCommits = numberLeftCommits;
	}

	public Integer getNumberRightCommits() {
		return numberRightCommits;
	}

	public void setNumberRightCommits(Integer numberRightCommits) {
		this.numberRightCommits = numberRightCommits;
	}

	public Integer getNumberDevelopers() {
		return numberDevelopers;
	}

	public void setNumberDevelopers(Integer numberDevelopers) {
		this.numberDevelopers = numberDevelopers;
	}

	public Integer getNumberLeftDevelopers() {
		return numberLeftDevelopers;
	}

	public void setNumberLeftDevelopers(Integer numberLeftDevelopers) {
		this.numberLeftDevelopers = numberLeftDevelopers;
	}

	public Integer getNumberRightDevelopers() {
		return numberRightDevelopers;
	}

	public void setNumberRightDevelopers(Integer numberRightDevelopers) {
		this.numberRightDevelopers = numberRightDevelopers;
	}

	public Integer getNumberBothSideDevelopers() {
		return numberBothSideDevelopers;
	}

	public void setNumberBothSideDevelopers(Integer numberBothSideDevelopers) {
		this.numberBothSideDevelopers = numberBothSideDevelopers;
	}
	
	public Integer getLoc() {
		return loc;
	}

	public void setLoc(Integer loc) {
		this.loc = loc;
	}

}
