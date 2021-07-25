package commnet.model.beans;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import gitwrapper.repo.Commit;

public class MergeScenario {

	private Integer idDB;
	private Integer mergeCommitIdDB;
	private Integer baseCommitIdDB;
	private Integer projectIdDB;

	private String mergeCommitHash;

	private Commit mergeCommit;
	private Commit baseCommit;
	private Commit leftParentCommit;
	private Commit rightParentCommit;
	private boolean hasConflict;
	private Date mergeCommitDate;
	private Date baseCommitDate;

	private List<FileMS> listFileMS = new ArrayList<FileMS>();
	private List<Commit> listCommitMergeScenario = new ArrayList<Commit>();
	private Map<String, List<DeveloperNode>> leftMergeDeveloperMap = new HashMap<String, List<DeveloperNode>>();
	private Map<String, List<DeveloperNode>> rightMergeDeveloperMap = new HashMap<String, List<DeveloperNode>>();
	private List<DeveloperEdge> mergeDevEdges = new ArrayList<>();

	private List<MergeConflictInfo> mergeConflictIntoList = new ArrayList<>();

	public MergeScenario() {

	}

	public MergeScenario(Integer projectIdDB, Commit mergeCommit) {
		setProjectidDB(projectIdDB);
		setMergeCommit(mergeCommit);
		setMergeDate(mergeCommit);
		setMergeCommitHash(mergeCommit.getId());
		setLeftParentCommit(mergeCommit.getParents().get().get(0));
		setRightParentCommit(mergeCommit.getParents().get().get(1));
		Optional<Commit> base = getLeftParentCommit().getMergeBase(getRightParentCommit());
		if (base.isPresent()) {
			setBaseCommit(base.get());
			getListCommitMergeScenario().add(getBaseCommit());
		}
		getListCommitMergeScenario().add(mergeCommit);
	}

	public MergeScenario(Integer idDB, Integer projectIdDB, Integer commitBase, Integer commitMerge,
			boolean hasConflict) {
		setIdDB(idDB);
		setProjectidDB(projectIdDB);
	}

	public Commit getMergeCommit() {
		return mergeCommit;
	}

	public void setMergeCommit(Commit mergeCommit) {
		this.mergeCommit = mergeCommit;
	}

	public Commit getBaseCommit() {
		return baseCommit;
	}

	public void setBaseCommit(Commit baseCommit) {
		setBaseDate(baseCommit);
		this.baseCommit = baseCommit;
	}

	public Commit getLeftParentCommit() {
		return leftParentCommit;
	}

	public void setLeftParentCommit(Commit leftParentCommit) {
		this.leftParentCommit = leftParentCommit;
	}

	public Commit getRightParentCommit() {
		return rightParentCommit;
	}

	public void setRightParentCommit(Commit rightParentCommit) {
		this.rightParentCommit = rightParentCommit;
	}

	public boolean getHasConflict() {
		return hasConflict;
	}

	public void setHasConflict(boolean hasConflict) {
		this.hasConflict = hasConflict;

	}

	public Date getMergeDate() {
		return mergeCommitDate;
	}

	public void setMergeDate(Date date) {
		this.mergeCommitDate = date;
	}

	public void setMergeDate(Commit mergeCommit) {
		if (mergeCommit != null && mergeCommit.getAuthorTime() != null) {
			OffsetDateTime aux = mergeCommit.getAuthorTime();
			LocalDateTime dateToConvert = aux.toLocalDateTime();
			java.util.Date convertToDate = Date.from(
					dateToConvert.atOffset(aux.getOffset()).atZoneSameInstant(ZoneId.systemDefault()).toInstant());
			this.mergeCommitDate = new Date(convertToDate.getTime());
		}
	}

	public Date getBaseDate() {
		return baseCommitDate;
	}

	public void setBaseDate(Date date) {
		this.baseCommitDate = date;
	}

	public void setBaseDate(Commit baseCommit) {
		OffsetDateTime aux = baseCommit.getAuthorTime();
		LocalDateTime dateToConvert = aux.toLocalDateTime();
		java.util.Date convertToDate = Date
				.from(dateToConvert.atOffset(aux.getOffset()).atZoneSameInstant(ZoneId.systemDefault()).toInstant());
		this.baseCommitDate = new Date(convertToDate.getTime());
	}

	public List<FileMS> getListFileMS() {
		return listFileMS;
	}

	public void setListFileMS(List<FileMS> listFileMS) {
		this.listFileMS = listFileMS;
	}

	public List<Commit> getListCommitMergeScenario() {
		return listCommitMergeScenario;
	}

	public void setListCommitMergeScenario(List<Commit> listCommitMergeScenario) {
		this.listCommitMergeScenario = listCommitMergeScenario;
	}

	public Map<String, List<DeveloperNode>> getLeftMergeDeveloperMap() {
		return leftMergeDeveloperMap;
	}

	public void setLeftMergeDeveloperMap(Map<String, List<DeveloperNode>> leftMergeDeveloperMap) {
		this.leftMergeDeveloperMap = leftMergeDeveloperMap;
	}

	public Map<String, List<DeveloperNode>> getRightMergeDeveloperMap() {
		return rightMergeDeveloperMap;
	}

	public void setRightMergeDeveloperMap(Map<String, List<DeveloperNode>> rightMergeDeveloperMap) {
		this.rightMergeDeveloperMap = rightMergeDeveloperMap;
	}

	public List<DeveloperEdge> getMergeDevEdges() {
		return mergeDevEdges;
	}

	public void setMergeDevEdges(List<DeveloperEdge> mergeDevEdges) {
		this.mergeDevEdges = mergeDevEdges;
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer id) {
		this.idDB = id;
	}

	public Integer getMergeCommitIdDB() {
		return mergeCommitIdDB;
	}

	public void setMergeCommitIdDB(Integer mergeCommitIdDB) {
		this.mergeCommitIdDB = mergeCommitIdDB;
	}

	public Integer getBaseCommitIdDB() {
		return baseCommitIdDB;
	}

	public void setBaseCommitIdDB(Integer baseCommitIdDB) {
		this.baseCommitIdDB = baseCommitIdDB;
	}

	public Integer getProjectidDB() {
		return projectIdDB;
	}

	public void setProjectidDB(Integer projectidDB) {
		this.projectIdDB = projectidDB;
	}

	public String getMergeCommitHash() {
		return mergeCommitHash;
	}

	public void setMergeCommitHash(String mergeCommitHash) {
		this.mergeCommitHash = mergeCommitHash;
	}

	public List<MergeConflictInfo> getMergeConflictIntoList() {
		return mergeConflictIntoList;
	}

	public void setMergeConflictIntoList(List<MergeConflictInfo> mergeConflictIntoList) {
		this.mergeConflictIntoList = mergeConflictIntoList;
	}

}
