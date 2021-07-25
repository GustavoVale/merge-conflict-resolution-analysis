package commnet.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commnet.model.beans.Chunk;
import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.FileMS;
import commnet.model.beans.MergeScenario;
import commnet.model.enums.EdgeSide;

public class ContributionEdgesBuilder {
	
	private List<MergeScenario> mergeScenariosList = new ArrayList<>();
	
	public ContributionEdgesBuilder(List<MergeScenario> mergeList) {
		setMergeScenariosList(mergeList);
		getMergeScenarioEdgesFromDataBase(mergeList);
	}
	
	public List<MergeScenario> getMergeScenariosList() {
		return mergeScenariosList;
	}

	public void setMergeScenariosList(List<MergeScenario> mergeScenariosList) {
		this.mergeScenariosList = mergeScenariosList;
	}

	public List<MergeScenario> getMergeScenarioEdgesFromDataBase(List<MergeScenario> mergeList) {

		for (MergeScenario merge : mergeList) {
			for (FileMS file : merge.getListFileMS()) {
				for (Chunk<Object> chunk : file.getChunkList()) {

					// GETTING EDGES FOR BOTH SIDES - Type 1
					chunk.getDevEdges().addAll(getIntraChunkDeveloperEdges(chunk, EdgeSide.LEFT));
					chunk.getDevEdges().addAll(getIntraChunkDeveloperEdges(chunk, EdgeSide.RIGHT));

					// GETTING CROSS-SIDE EDGES - Type 2
					chunk.getDevEdges().addAll(getIntraChunkDeveloperCrossSideEdges(chunk));

					file.getLeftFileDeveloperMap().put(chunk.getIdDB(), chunk.getLeftDevList());
					file.getRightFileDeveloperMap().put(chunk.getIdDB(), chunk.getRightDevList());
				}

				// GETTING EDGES FOR BOTH SIDES AT FILE LEVEL - Type 3
				List<DeveloperEdge> leftEdges = getIntraFileDeveloperEdges(file, EdgeSide.LEFT);
				if (leftEdges != null) {
					file.getFileDevEdges().addAll(leftEdges);
				}
				List<DeveloperEdge> rightEdges = getIntraFileDeveloperEdges(file, EdgeSide.RIGHT);
				if (rightEdges != null) {
					file.getFileDevEdges().addAll(rightEdges);
				}
				// GETTING CROSS-SIDE EDGES - Type 4
				List<DeveloperEdge> bothEdges = getIntraFileDeveloperCrossSideEdges(file);
				if (bothEdges != null && !bothEdges.isEmpty()) {
					file.getFileDevEdges().addAll(bothEdges);
				}

				if (!file.getLeftFileDeveloperMap().isEmpty()) {
					merge.getLeftMergeDeveloperMap().put(file.getFileName(), file.getLeftFileDeveloperList());
				}
				if (!file.getRightFileDeveloperMap().isEmpty()) {
					merge.getRightMergeDeveloperMap().put(file.getFileName(), file.getRightFileDeveloperList());
				}
			}

			// Continue the Merge-level network
			// GETTING EDGES FOR BOTH SIDES AT MERGE LEVEL - Type 5
			List<DeveloperEdge> leftEdges = getIntraMergeDeveloperEdges(merge, EdgeSide.LEFT);
			if (leftEdges != null) {
				merge.getMergeDevEdges().addAll(leftEdges);
			}
			List<DeveloperEdge> rightEdges = getIntraMergeDeveloperEdges(merge, EdgeSide.RIGHT);
			if (rightEdges != null) {
				merge.getMergeDevEdges().addAll(rightEdges);
			}
			// GETTING CROSS-SIDE EDGES - Type 6
			List<DeveloperEdge> bothEdges = getIntraMergeDeveloperCrossSideEdges(merge);
			if (bothEdges != null && !bothEdges.isEmpty()) {
				merge.getMergeDevEdges().addAll(bothEdges);
			}

		}

		return mergeList;
	}
	
	/**
	 * Get Intra-Chunk Edges. It consists of our edge type 1
	 * 
	 * @param chunk
	 * @param side
	 * @return list of DeveloperEdges
	 */
	private List<DeveloperEdge> getIntraChunkDeveloperEdges(Chunk<?> chunk, EdgeSide side) {

		List<DeveloperEdge> edgesList = new ArrayList<DeveloperEdge>();
		List<DeveloperNode> developerList = new ArrayList<>();
		if (side.equals(EdgeSide.LEFT)) {
			developerList = chunk.getLeftDevList();
		} else {
			developerList = chunk.getRightDevList();
		}

		if (developerList.size() == 1) {
			DeveloperEdge newEdge = new DeveloperEdge(developerList.get(0), developerList.get(0), 1, side, 1);
			if (!edgesList.contains(newEdge)) {
				edgesList.add(newEdge);
			}
		} else {

			for (DeveloperNode devNode : developerList) {
				for (DeveloperNode devNode2 : developerList) {
					if (devNode.equals(devNode2)) {
						continue;
					}

					DeveloperEdge newEdge = new DeveloperEdge(devNode, devNode2, 1, side, 1);
					if (!edgesList.contains(newEdge)) {
						edgesList.add(newEdge);
					}
				}

			}
		}
		return edgesList;
	}
	
	/**
	 * Get Intra-Chunk Cross Side Edges. It consists of our edge type 2
	 * 
	 * @param chunk
	 * @return list of DeveloperEdges
	 */
	private List<DeveloperEdge> getIntraChunkDeveloperCrossSideEdges(Chunk<Object> chunk) {

		List<DeveloperEdge> edgesList = new ArrayList<DeveloperEdge>();
		List<DeveloperNode> leftList = chunk.getLeftDevList();
		List<DeveloperNode> rightList = chunk.getRightDevList();

		if (!leftList.equals(null) || !rightList.equals(null)) {
			for (DeveloperNode devNode : leftList) {
				for (DeveloperNode devNode2 : rightList) {
					DeveloperEdge newEdge = new DeveloperEdge(devNode, devNode2, 2, EdgeSide.LEFTRIGHT, 1);
					if (!edgesList.contains(newEdge)) {
						edgesList.add(newEdge);
					}
				}
			}
		}
		return edgesList;
	}
	
	/**
	 * Get Intra-File Edges. It consists of our edge type 3
	 * 
	 * @param newFile
	 * @param side
	 * @return
	 */
	private List<DeveloperEdge> getIntraFileDeveloperEdges(FileMS newFile, EdgeSide side) {
		List<DeveloperEdge> edgesList = new ArrayList<DeveloperEdge>();
		Map<Integer, List<DeveloperNode>> auxMap = new HashMap<Integer, List<DeveloperNode>>();
		Map<Integer, List<DeveloperNode>> fileDeveloperMap = new HashMap<Integer, List<DeveloperNode>>();
		if (side.equals(EdgeSide.LEFT)) {
			fileDeveloperMap.putAll(newFile.getLeftFileDeveloperMap());
		} else {
			fileDeveloperMap.putAll(newFile.getRightFileDeveloperMap());
		}
		auxMap.putAll(fileDeveloperMap);

		if (fileDeveloperMap.size() == 1 || fileDeveloperMap.isEmpty()) {
			return null;
		} else {
			for (Integer i : fileDeveloperMap.keySet()) {
				List<DeveloperNode> currMap = fileDeveloperMap.get(i);
				auxMap.remove(i);
				for (DeveloperNode currI : currMap) {
					for (Integer j : auxMap.keySet()) {
						if (i != j) {
							for (DeveloperNode currJ : fileDeveloperMap.get(j)) {
								DeveloperEdge newEdge = new DeveloperEdge(currI, currJ, 3, side, 1);
								if (!edgesList.contains(newEdge)) {
									edgesList.add(newEdge);
								} else {
									for (DeveloperEdge oldEdge : edgesList) {
										if (oldEdge.equals(newEdge)) {
											oldEdge.setWeight(oldEdge.getWeight() + 1);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return edgesList;
	}
	
	/**
	 * Get Intra-File Cross Side Edges. It consists of our edge type 4
	 * 
	 * @param newFile
	 * @return list of DeveloperEdges
	 */
	private List<DeveloperEdge> getIntraFileDeveloperCrossSideEdges(FileMS newFile) {

		List<DeveloperEdge> edgesList = new ArrayList<DeveloperEdge>();

		if (newFile.getLeftFileDeveloperMap().equals(null) || newFile.getRightFileDeveloperMap().equals(null)) {
			return null;
		} else {
			for (Integer i : newFile.getLeftFileDeveloperMap().keySet()) {
				List<DeveloperNode> currList = newFile.getLeftFileDeveloperMap().get(i);
				for (DeveloperNode currI : currList) {
					for (Integer j : newFile.getRightFileDeveloperMap().keySet()) {
						if (i != j) {
							for (DeveloperNode currJ : newFile.getRightFileDeveloperMap().get(j)) {
								DeveloperEdge newEdge = new DeveloperEdge(currI, currJ, 4, EdgeSide.LEFTRIGHT, 1);
								if (!edgesList.contains(newEdge)) {
									edgesList.add(newEdge);
								} else {
									for (DeveloperEdge oldEdge : edgesList) {
										if (oldEdge.equals(newEdge)) {
											oldEdge.setWeight(oldEdge.getWeight() + 1);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return edgesList;
	}
	
	/**
	 * Get Intra-Merge Edges. It consists of our edge type 5
	 * 
	 * @param newMergeScenario
	 * @param side
	 * @return list of developerEdges
	 */
	private List<DeveloperEdge> getIntraMergeDeveloperEdges(MergeScenario newMergeScenario, EdgeSide side) {
		List<DeveloperEdge> edgesList = new ArrayList<DeveloperEdge>();
		Map<String, List<DeveloperNode>> auxMap = new HashMap<String, List<DeveloperNode>>();
		Map<String, List<DeveloperNode>> mergeDeveloperMap = new HashMap<String, List<DeveloperNode>>();
		if (side.equals(EdgeSide.LEFT)) {
			mergeDeveloperMap.putAll(newMergeScenario.getLeftMergeDeveloperMap());
		} else {
			mergeDeveloperMap.putAll(newMergeScenario.getRightMergeDeveloperMap());
		}
		auxMap.putAll(mergeDeveloperMap);

		if (mergeDeveloperMap.size() == 1 || mergeDeveloperMap.size() == 0) {
			return null;
		} else {
			for (String i : mergeDeveloperMap.keySet()) {
				List<DeveloperNode> currMap = mergeDeveloperMap.get(i);
				auxMap.remove(i);
				for (DeveloperNode currI : currMap) {
					for (String j : auxMap.keySet()) {
						if (i != j) {
							for (DeveloperNode currJ : mergeDeveloperMap.get(j)) {
								DeveloperEdge newEdge = new DeveloperEdge(currI, currJ, 5, side, 1);
								if (!edgesList.contains(newEdge)) {
									edgesList.add(newEdge);
								} else {
									for (DeveloperEdge oldEdge : edgesList) {
										if (oldEdge.equals(newEdge)) {
											oldEdge.setWeight(oldEdge.getWeight() + 1);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return edgesList;
	}
	
	/**
	 * Get Intra-Merge Scenario Cross Side Edges, it consist of our edge type 6
	 * 
	 * @param newMergeScenario
	 * @return list of DeveloperEdges
	 */
	private List<DeveloperEdge> getIntraMergeDeveloperCrossSideEdges(MergeScenario newMergeScenario) {

		List<DeveloperEdge> edgesList = new ArrayList<DeveloperEdge>();

		if (newMergeScenario.getLeftMergeDeveloperMap().size() == 1
				&& newMergeScenario.getRightMergeDeveloperMap().size() == 1
				&& newMergeScenario.getLeftMergeDeveloperMap().keySet()
						.equals(newMergeScenario.getRightMergeDeveloperMap().keySet())) {
			return null;
		} else {
			for (String i : newMergeScenario.getLeftMergeDeveloperMap().keySet()) {
				List<DeveloperNode> currList = newMergeScenario.getLeftMergeDeveloperMap().get(i);
				for (DeveloperNode currI : currList) {
					for (String j : newMergeScenario.getRightMergeDeveloperMap().keySet()) {
						if (i != j) {
							for (DeveloperNode currJ : newMergeScenario.getRightMergeDeveloperMap().get(j)) {
								DeveloperEdge newEdge = new DeveloperEdge(currI, currJ, 6, EdgeSide.LEFTRIGHT, 1);
								if (!edgesList.contains(newEdge)) {
									edgesList.add(newEdge);
								} else {
									for (DeveloperEdge oldEdge : edgesList) {
										if (oldEdge.equals(newEdge)) {
											oldEdge.setWeight(oldEdge.getWeight() + 1);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return edgesList;
	}
}
