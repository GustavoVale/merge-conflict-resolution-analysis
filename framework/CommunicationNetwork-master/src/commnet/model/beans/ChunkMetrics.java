package commnet.model.beans;

public class ChunkMetrics {

	private Integer IdDB;
	private Integer chunkIdDB;

	private Integer numberCommits;
	private Integer numberLeftCommits;
	private Integer numberRightCommits;

	private Integer numberDevelopers;
	private Integer numberLeftDevelopers;
	private Integer numberRightDevelopers;
	private Integer numberBothSideDevelopers;

	public ChunkMetrics(Integer chunkIdDB) {
		this(null, chunkIdDB, null, null, null, null, null, null, null);
	}

	public ChunkMetrics(Integer idDB, Integer chunkIdDB) {
		this(idDB, chunkIdDB, null, null, null, null, null, null, null);
	}

	public ChunkMetrics(Integer idDB, Integer chunkIdDB, Integer nco, Integer nlco, Integer nrco, Integer ndev,
			Integer nldev, Integer nrdev, Integer nbdev) {
		setIdDB(idDB);
		setChunkIdDB(chunkIdDB);
		setNumberCommits(nco);
		setNumberLeftCommits(nlco);
		setNumberRightCommits(nrco);
		setNumberDevelopers(ndev);
		setNumberLeftDevelopers(nldev);
		setNumberRightDevelopers(nrdev);
		setNumberBothSideDevelopers(nbdev);

	}

	public Integer getIdDB() {
		return IdDB;
	}

	public void setIdDB(Integer idDB) {
		IdDB = idDB;
	}

	public Integer getChunkIdDB() {
		return chunkIdDB;
	}

	public void setChunkIdDB(Integer chunkIdDB) {
		this.chunkIdDB = chunkIdDB;
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

}
