package cvommnet.model.beans;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMS {

	private String fileName;
	private Path path;
	private boolean conflict;
	private List<Chunk<Object>> chunkList = new ArrayList<>();
	private Integer idDB;
	private Integer MergeScenarioIdDB;
	private Map<Integer, List<DeveloperNode>> leftFileDeveloperMap = new HashMap<Integer, List<DeveloperNode>>();
	private Map<Integer, List<DeveloperNode>> rightFileDeveloperMap = new HashMap<Integer, List<DeveloperNode>>();
	private List<DeveloperEdge> fileDevEdges = new ArrayList<DeveloperEdge>();

	public FileMS(){
		
	}
	
	public FileMS(Path filepath) {
		setFileName(filepath.toString());
		setPath(filepath);
	}

	public FileMS(Integer idDB, String fileName, Integer mergeScenarioIdDB, boolean hasConflict) {
		setIdDB(idDB);
		setFileName(fileName);
		setMergeScenarioIdDB(mergeScenarioIdDB);
		setConflict(hasConflict);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String path) {
		this.fileName = path.toString();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public List<Chunk<Object>> getChunkList() {
		return chunkList;
	}

	public void setChunkList(List<Chunk<Object>> chunkList) {
		this.chunkList = chunkList;
	}

	public Map<Integer, List<DeveloperNode>> getLeftFileDeveloperMap() {
		return leftFileDeveloperMap;
	}

	public void setLeftFileDeveloperMap(Map<Integer, List<DeveloperNode>> leftFileDeveloperMap) {
		this.leftFileDeveloperMap = leftFileDeveloperMap;
	}

	public Map<Integer, List<DeveloperNode>> getRightFileDeveloperMap() {
		return rightFileDeveloperMap;
	}

	public void setRightFileDeveloperMap(Map<Integer, List<DeveloperNode>> rightFileDeveloperMap) {
		this.rightFileDeveloperMap = rightFileDeveloperMap;
	}

	public List<DeveloperNode> getLeftFileDeveloperList() {
		List<DeveloperNode> devList = new ArrayList<>();
		for (Map.Entry<Integer, List<DeveloperNode>> entry : leftFileDeveloperMap.entrySet()) {
			List<DeveloperNode> devChunkList = entry.getValue();
			for (DeveloperNode dev : devChunkList) {
				if (!devList.contains(dev)) {
					devList.add(dev);
				}
			}
		}

		return devList;
	}

	public List<DeveloperNode> getRightFileDeveloperList() {
		List<DeveloperNode> devList = new ArrayList<>();
		for (Map.Entry<Integer, List<DeveloperNode>> entry : rightFileDeveloperMap.entrySet()) {
			List<DeveloperNode> devChunkList = entry.getValue();
			for (DeveloperNode dev : devChunkList) {
				if (!devList.contains(dev)) {
					devList.add(dev);
				}
			}
		}

		return devList;
	}

	public List<DeveloperEdge> getFileDevEdges() {
		return fileDevEdges;
	}

	public void setFileDevEdges(List<DeveloperEdge> fileDevEdges) {
		this.fileDevEdges = fileDevEdges;
	}

	public boolean isConflict() {
		return conflict;
	}

	public void setConflict(boolean conflict) {
		this.conflict = conflict;
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getMergeScenarioIdDB() {
		return MergeScenarioIdDB;
	}

	public void setMergeScenarioIdDB(Integer mergeScenarioIdDB) {
		MergeScenarioIdDB = mergeScenarioIdDB;
	}

}
