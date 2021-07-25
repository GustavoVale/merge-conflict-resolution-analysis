package commnet.model.beans;

public class MergeScenarioMetrics {

	private Integer IdDB;
	private Integer msIdDB;

	private Integer numberFiles;
	private Integer numberConflictedFiles;
	private Integer numberLeftFiles;
	private Integer numberRightFiles;
	private Integer numberBothSideFiles;

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
	
	private Integer codeChurn;
	private Integer conflictingCodeChurn;


	public MergeScenarioMetrics(Integer msIdDB) {
		this(null, msIdDB, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null);
	}

	public MergeScenarioMetrics(Integer idDB, Integer msIdDB) {
		this(idDB, msIdDB, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null);
	}

	public MergeScenarioMetrics(Integer idDB, Integer msIdDB, Integer nf, Integer ncf, Integer nlf, Integer nrf,
			Integer nbf, Integer nch, Integer ncch, Integer nlch, Integer nrch, Integer nco, Integer nlco, Integer nrco,
			Integer ndev, Integer nldev, Integer nrdev, Integer nbdev, Integer codeChurn, Integer conflictingCodeChurn) {
		setIdDB(idDB);
		setmsIdDB(msIdDB);
		setNumberFiles(nf);
		setNumberConflictedFiles(ncf);
		setNumberLeftFiles(nlf);
		setNumberRightFiles(nrf);
		setNumberBothSideFiles(nbf);
		setNumberChunks(nch);
		setNumberConflictedChunks(ncch);
		setNumberLeftChunks(nlch);
		setNumberRightChunks(nrch);
		setNumberCommits(nco);
		setNumberLeftCommits(nlco);
		setNumberRightCommits(nrco);
		setNumberDevelopers(ndev);
		setNumberLeftDevelopers(nldev);
		setNumberRightDevelopers(nrdev);
		setNumberBothSideDevelopers(nbdev);
		setCodeChurn(codeChurn);
		setConflictingCodeChurn(conflictingCodeChurn);
	}

	public Integer getIdDB() {
		return IdDB;
	}

	public void setIdDB(Integer idDB) {
		IdDB = idDB;
	}

	public Integer getMsIdDB() {
		return msIdDB;
	}

	public void setmsIdDB(Integer msIdDB) {
		this.msIdDB = msIdDB;
	}

	public Integer getNumberFiles() {
		return numberFiles;
	}

	public void setNumberFiles(Integer numberFiles) {
		this.numberFiles = numberFiles;
	}

	public Integer getNumberConflictedFiles() {
		return numberConflictedFiles;
	}

	public void setNumberConflictedFiles(Integer numberConflictedFiles) {
		this.numberConflictedFiles = numberConflictedFiles;
	}

	public Integer getNumberLeftFiles() {
		return numberLeftFiles;
	}

	public void setNumberLeftFiles(Integer numberLeftFiles) {
		this.numberLeftFiles = numberLeftFiles;
	}

	public Integer getNumberRightFiles() {
		return numberRightFiles;
	}

	public void setNumberRightFiles(Integer numberRightFiles) {
		this.numberRightFiles = numberRightFiles;
	}

	public Integer getNumberBothSideFiles() {
		return numberBothSideFiles;
	}

	public void setNumberBothSideFiles(Integer numberBothSideFiles) {
		this.numberBothSideFiles = numberBothSideFiles;
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

	public Integer getCodeChurn() {
		return codeChurn;
	}

	public void setCodeChurn(Integer codeChurn) {
		this.codeChurn = codeChurn;
	}
	
	public Integer getConflictingCodeChurn() {
		return conflictingCodeChurn;
	}

	public void setConflictingCodeChurn(Integer conflictingCodeChurn) {
		this.conflictingCodeChurn = conflictingCodeChurn;
	}

}
