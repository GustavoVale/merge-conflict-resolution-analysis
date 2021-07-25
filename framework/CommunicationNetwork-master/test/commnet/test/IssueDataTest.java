package commnet.test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class IssueDataTest {
	private Integer id;
	private String state;
	private Timestamp openDate;
	private Timestamp closeDate;
	private UserDataTest user;

	// Need to change to CommentDataTest
	private List<UserDataTest> commentsList = new ArrayList<>();
	// Need to change to EventDataTest
	private List<UserDataTest> eventsList = new ArrayList<>();

	public IssueDataTest(Integer id) {
		this(id, null, null, null, null, null);

	}

	public IssueDataTest(Integer id, Timestamp openDate, Timestamp closeDate, String IssueState) {
		this(id, openDate, closeDate, null, null, null);
	}

	public IssueDataTest(Integer id, Timestamp openDate, Timestamp closeDate, UserDataTest user,
			List<UserDataTest> commentsList, List<UserDataTest> eventList) {
		setId(id);
		setUser(user);
		setOpenDate(openDate);
		setCloseDate(closeDate);
		setState(closeDate);
		setCommentsList(commentsList);
		setEventsList(eventList);
	}

	// I need to change the parameter to events
	public List<UserDataTest> getEventsList() {
		return eventsList;
	}

	// I need to change the parameter to events
	private void setEventsList(List<UserDataTest> eventList) {
		this.eventsList = eventList;

	}

	// I need to change the parameter to comments
	public List<UserDataTest> getCommentsList() {
		return commentsList;
	}

	// I need to change parameter to comments
	private void setCommentsList(List<UserDataTest> commentsList) {
		this.commentsList = commentsList;

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(Timestamp closeDate) {
		if (closeDate == null)
			this.state = "Closed";
		else
			this.state = "Opened";
	}

	public Timestamp getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Timestamp openDate) {
		this.openDate = openDate;
	}

	public Timestamp getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Timestamp closeDate) {
		this.closeDate = closeDate;
	}

	public UserDataTest getUser() {
		return user;
	}

	public void setUser(UserDataTest user) {
		this.user = user;
	}
}
