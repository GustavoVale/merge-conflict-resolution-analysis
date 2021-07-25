package commnet.model.beans;

import java.util.ArrayList;
import java.util.List;

public class Network {

	private Integer idDB;
	private Integer msIdDB;
	private MergeScenario mergeScenario;
	private List<DeveloperEdge> relatedDevEdges;
	private List<DeveloperNode> relatedDevs;

	public Network() {
		this(null, null, null, null, null, null);
	}

	public Network(Integer idDB, MergeScenario mergeScenario) {
		setIdDB(idDB);
		setMergeScenario(mergeScenario);
	}

	public Network(Integer id, Integer msIdDB) {
		setIdDB(id);
		setmsIdDB(msIdDB);
	}

	public Network(Integer msIdDB, MergeScenario mergeScenario, List<DeveloperNode> devList, List<DeveloperEdge> devEdges) {
		this(null, null, msIdDB, mergeScenario, devList, devEdges);

	}

	public Network(Integer projectIdDB, Integer msIdDB, MergeScenario mergeScenario, List<DeveloperEdge> devEdges) {
		this(null, projectIdDB, msIdDB, mergeScenario, null, devEdges);
	}

	public Network(Integer idDB, Integer projectID, Integer msIdDB, MergeScenario mergeScenario, List<DeveloperNode> devList,
			List<DeveloperEdge> devEdges) {
		setmsIdDB(msIdDB);
		setMergeScenario(mergeScenario);
		setDevEdges(devEdges);
	}

	public void setDevListByEdges() {
		List<DeveloperNode> devList = new ArrayList<DeveloperNode>();
		for (DeveloperEdge devEdge : relatedDevEdges) {
			if (!devList.contains(devEdge.getDevA())) {
				devList.add(devEdge.getDevA());
			}
			if (!devList.contains(devEdge.getDevB())) {
				devList.add(devEdge.getDevB());
			}
		}
		if (getDevList() == null) {
			this.relatedDevs = devList;
		} else {
			this.relatedDevs.addAll(devList);
		}
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getIdDB() {
		return this.idDB;
	}

	public void setDevEdges(List<DeveloperEdge> devEdges) {
		this.relatedDevEdges = devEdges;

	}

	public List<DeveloperEdge> getDevEdges() {
		return relatedDevEdges;

	}

	public void setDevList(List<DeveloperNode> devs) {
		this.relatedDevs = devs;

	}

	public List<DeveloperNode> getDevList() {
		return relatedDevs;

	}

	public void setmsIdDB(Integer mergeScenarioID) {
		this.msIdDB = mergeScenarioID;
	}

	public Integer getmsIdDB() {
		return this.msIdDB;
	}

	public void setMergeScenario(MergeScenario mergeScenario) {
		this.mergeScenario = mergeScenario;
	}

	public MergeScenario getMergeScenario() {
		return this.mergeScenario;
	}

	public void addEdge(DeveloperEdge developerEdge) {
		if (this.relatedDevEdges == null) {
			this.relatedDevEdges = new ArrayList<>();
		}
		this.relatedDevEdges.add(developerEdge);
	}

}
