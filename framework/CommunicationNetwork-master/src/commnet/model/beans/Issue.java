package commnet.model.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import gitwrapper.repo.Commit;

public class Issue {

	private Integer idDB;
	private Integer projectID;
	private Integer issueID;
	private boolean isPullRequest;
	private Commit mergecommit;
	private String pullHash = null;

	private boolean isClosed;
	private Date createdAt;
	private Date closedAt;
	private List<Event> relatedEvents = new ArrayList<>();
	private List<DeveloperNode> devList = new ArrayList<>();

	public List<Commit> relatedCommits = new ArrayList<>();
	private List<Integer> relatedIssues = new ArrayList<>();
	private List<String> relatedLabels = new ArrayList<>();

	public Issue() {
		this(null, null, null, null, null, false, false);
	}

	public Issue(Integer projectIdDB, Integer issueIdDB) {
		this(null, projectIdDB, issueIdDB, null, null, false, false);
	}

	public Issue(Integer id, Integer projectID, Integer issueId, Date openedAt, Date closedAt, boolean type,
			boolean state) {
		setIdDB(id);
		setProjectID(projectID);
		setIssueID(issueId);
		setOpenedDate(openedAt);
		setClosedDate(closedAt);
		setIsPullRequest(type);
		setIsClosed(state);
	}

	public void addEvent(Event event) {
		relatedEvents.add(event);
	}

	public void addRelatedCommit(Commit commit) {
		relatedCommits.add(commit);
	}

	public void addRelatedIssue(Integer relatedIssue) {
		relatedIssues.add(relatedIssue);
	}

	public void addRelatedLabel(String relatedLabel) {
		relatedLabels.add(relatedLabel);
	}

	public List<String> getRelatedLabels() {
		return relatedLabels;
	}

	public void setRelatedLabels(List<String> relatedLabels) {
		this.relatedLabels = relatedLabels;
	}

	public List<Commit> getRelatedCommits() {
		return Collections.unmodifiableList(relatedCommits);
	}

	public List<Integer> getRelatedIssues() {
		return relatedIssues;
	}

	public void setRelatedIssues(List<Integer> relatedIssuesList) {
		this.relatedIssues = relatedIssuesList;
	}

	public List<Event> getRelatedEvents() {
		return relatedEvents;
	}

	public void setRelatedEvents(List<Event> eventList) {
		this.relatedEvents = eventList;
	}

	public boolean getIsPullRequest() {
		return isPullRequest;
	}

	public void setIsPullRequest(String isPullRequest) {
		if (isPullRequest == "true")
			this.isPullRequest = true;
		else
			this.isPullRequest = false;
	}

	public void setIsPullRequest(boolean type) {
		this.isPullRequest = type;
	}

	public boolean getIsClosed() {
		return isClosed;
	}

	public void setIsClosed(boolean state2) {
		this.isClosed = state2;
	}

	public void setIdDB(Integer id) {
		this.idDB = id;
	}

	public Integer getIdDB() {
		return this.idDB;
	}

	public void setIssueID(Integer issueId) {
		this.issueID = issueId;
	}

	public Integer getIssueID() {
		return this.issueID;
	}

	public void setProjectID(Integer projectID) {
		this.projectID = projectID;
	}

	public Integer getProjectID() {
		return this.projectID;
	}

	public Date getOpenedDate() {
		return createdAt;
	}

	public void setOpenedDate(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getClosedDate() {
		return closedAt;
	}

	public void setClosedDate(Date closedAt) {
		this.closedAt = closedAt;
	}

	public Commit getMergeCommit() {
		return mergecommit;
	}

	public void setMergeCommit(Commit mergeCommit) {
		this.mergecommit = mergeCommit;
	}

	public List<DeveloperNode> getDevList() {
		return devList;
	}

	public void setDevList(List<DeveloperNode> devList) {
		this.devList = devList;
	}

	public void addDev(DeveloperNode developerNode) {
		if (this.devList == null) {
			this.devList = new ArrayList<>();
		}
		this.devList.add(developerNode);
	}

	public String getPullHash() {
		return pullHash;
	}

	public void setPullHash(String pullHash) {
		this.pullHash = pullHash;
	}

	public void setDevListByEvents() {
		List<DeveloperNode> newDevList = new ArrayList<>();
		for (Event event : relatedEvents) {
			if (!newDevList.contains(event.getCont())) {
				newDevList.add(event.getCont());
			}
		}
		devList.addAll(newDevList);
	}

}
