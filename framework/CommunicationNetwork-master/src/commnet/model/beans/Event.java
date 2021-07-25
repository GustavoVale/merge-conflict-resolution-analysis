package commnet.model.beans;

import java.util.Date;

public class Event {

	private Integer idDB;
	private Integer issueIdDB;
	private Integer issueID;
	private Date createdAt;
	private DeveloperNode cont;
	private boolean isCommentary;

	public Event(){
		
	}
	
	public Event(Integer issueId, Date openedAt, DeveloperNode contributor, boolean isComent) {

		setIssueID(issueId);
		setCreatedAt(openedAt);
		setCont(contributor);
		setCommentary(isComent);
	}

	public Event(Integer id, Integer issueIdDB, Integer contributorIdDB, boolean isComment, Date openedAt) {
		setIdDB(id);
		setIssueIdDB(issueIdDB);
		setCommentary(isComment);
		setCreatedAt(openedAt);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer id) {
		this.idDB = id;
	}

	public Integer getIssueIdDB() {
		return issueIdDB;
	}

	public void setIssueIdDB(Integer issueIdDB) {
		this.issueIdDB = issueIdDB;
	}

	public Integer getIssueID() {
		return issueID;
	}

	public void setIssueID(Integer issueID) {
		this.issueID = issueID;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public DeveloperNode getCont() {
		return cont;
	}

	public void setCont(DeveloperNode dev) {
		this.cont = dev;
	}

	public boolean isCommentary() {
		return isCommentary;
	}

	public void setCommentary(boolean isCommentary) {
		this.isCommentary = isCommentary;
	}
}
