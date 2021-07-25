package commnet.model.beans;

public class DeveloperRole {
	private Integer idDB;
	private Integer mergeScenarioIdDB;
	private Integer contributorIdDB;
	
	//just for communicators
	private boolean isComprehensive = false;
	
	public DeveloperRole (Integer idDB, Integer msIdDB, Integer contIdDB){
		this(idDB, msIdDB, contIdDB, false);
	}
	
	public DeveloperRole (Integer idDB, Integer msIdDB, Integer contIdDB, boolean isComprehensive){
		setIdDB(idDB);
		setMergeScenarioIdDB(msIdDB);
		setContributorIdDB(contIdDB);
		this.isComprehensive = isComprehensive;
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
	
	public boolean isComprehensive() {
		return isComprehensive;
	}

	public void setComprehensive(boolean isComprehensive) {
		this.isComprehensive = isComprehensive;
	}
}
