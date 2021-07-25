package commnet.model.beans;

import java.util.ArrayList;
import java.util.List;

import gitwrapper.repo.Commit;

public class Chunk<T> {

	private Integer id;
	private Integer beginLine;
	private Integer endLine;
	private boolean isConflict;
	
	private StringBuilder leftCode;
	private StringBuilder rightCode;
	
	private Integer idDB;
	private Integer fileIdDB;

	private List<Commit> leftCommitList = new ArrayList<>();
	private List<Commit> rightCommitList = new ArrayList<>();

	private List<DeveloperNode> leftDevList = new ArrayList<DeveloperNode>();
	private List<DeveloperNode> rightDevList = new ArrayList<DeveloperNode>();
	private List<DeveloperEdge> devEdges = new ArrayList<DeveloperEdge>();

	public Chunk() {
	}

	public Chunk(Integer idDB, Integer fileIdDB, Integer lineBegin, Integer lineEnd, boolean hasConflict) {
		this(idDB, fileIdDB, lineBegin, lineEnd, hasConflict, "", "");
	}
	
	public Chunk(Integer idDB, Integer fileIdDB, Integer lineBegin, Integer lineEnd, boolean hasConflict, String leftCode, String rightCode) {
		setIdDB(idDB);
		setFileIdDB(fileIdDB);
		setBeginLine(lineBegin);
		setEndLine(lineEnd);
		isConflict(hasConflict);
	}

	public Integer getID() {
		return id;
	}

	public void setID(Integer id) {
		this.id = id;
	}

	public Integer getBeginLine() {
		return beginLine;
	}

	public void setBeginLine(Integer line) {
		this.beginLine = line;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public void setEndLine(Integer line) {
		this.endLine = line;
	}

	public void isConflict(boolean conflictedChunk) {
		this.isConflict = conflictedChunk;
	}

	public boolean getIfIsConflict() {
		return isConflict;
	}

	public void setRightCommitList(List<Commit> commitList) {
		this.rightCommitList = commitList;
	}

	public List<Commit> getRightCommitList() {
		return rightCommitList;
	}

	public void setLeftCommitList(List<Commit> commitList) {
		this.leftCommitList = commitList;
	}

	public List<Commit> getLeftCommitList() {
		return leftCommitList;
	}

	public List<DeveloperNode> getLeftDevList() {
		return leftDevList;
	}

	public void setLeftDevList(List<DeveloperNode> leftDevList) {
		this.leftDevList = leftDevList;
	}

	public List<DeveloperNode> getRightDevList() {
		return rightDevList;
	}

	public void setRightDevList(List<DeveloperNode> rightDevList) {
		this.rightDevList = rightDevList;
	}

	public List<DeveloperEdge> getDevEdges() {
		return devEdges;
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getFileIdDB() {
		return fileIdDB;
	}

	public void setFileIdDB(Integer fileIdDB) {
		this.fileIdDB = fileIdDB;
	}

	public StringBuilder getLeftCode() {
		return leftCode;
	}
	
	public void setLeftCode(StringBuilder leftCode) {
		this.leftCode = leftCode;
	}

	public StringBuilder getRightCode() {
		return rightCode;
	}

	public void setRightCode(StringBuilder rightCode) {
		this.rightCode = rightCode;
	}

}
