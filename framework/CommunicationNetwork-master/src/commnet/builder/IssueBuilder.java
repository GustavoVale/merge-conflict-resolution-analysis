package commnet.builder;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import githubinterface.GitHubRepository;
import githubinterface.PullRequest;
import githubinterface.datadefinitions.CommentData;
import githubinterface.datadefinitions.EventData;
import githubinterface.datadefinitions.IssueData;
import githubinterface.datadefinitions.State;
import gitwrapper.repo.Commit;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.Event;
import commnet.model.beans.Issue;
import commnet.model.beans.Project;
import commnet.model.dao.IssueDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class IssueBuilder implements Runnable {

	protected File log;
	protected GitHubRepository gitHubRepo;
	private List<Issue> issuesList = new ArrayList<Issue>();

	private boolean simpleDatabaseSearch;
	private Project project;

	public IssueBuilder(GitHubRepository gitHubRepo, Project project, boolean simpleDatabaseSearch) {
		setGitHubRepo(gitHubRepo);
		setProject(project);
		setSimpleDatabaseSearch(simpleDatabaseSearch);
	}

	public void setGitHubRepo(GitHubRepository gitHubRepository) {
		this.gitHubRepo = gitHubRepository;
	}

	public void setLogFile(File log) {
		this.log = log;
	}

	public List<Issue> getIssuesList() {
		return issuesList;
	}

	public void setIssuesList(List<Issue> issuesList) {
		this.issuesList = issuesList;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public boolean isSimpleDatabaseSearch() {
		return simpleDatabaseSearch;
	}

	public void setSimpleDatabaseSearch(boolean simpleDatabaseSearch) {
		this.simpleDatabaseSearch = simpleDatabaseSearch;
	}

	public void run() {
		try {
			if (!isSimpleDatabaseSearch()) {
				setIssuesList(getIssues());
			} else {
				setIssuesList(buildIssuesFromDatabase());
			}
		} catch (IOException e) {
			Logger.logStackTrace(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Build issues of a project based on GitHubWrapper information
	 * 
	 * @return list of Issues
	 * @throws IOException
	 */
	public List<Issue> getIssues() throws IOException {

		Logger.log(log, "Issue building start.");

		Optional<List<IssueData>> issueDataList = gitHubRepo.getIssues(true);
		Optional<List<PullRequest>> pullList = Optional.of(new ArrayList<PullRequest>());

		try {
			pullList = gitHubRepo.getPullRequests(State.ANY);
		} catch (NullPointerException e) {
			e.printStackTrace();
			Logger.log(log, "ERROR Getting Pull Requests");
		}

		List<Issue> resultIssueList = new ArrayList<Issue>();
		List<Integer> issueGitHubId = new ArrayList<>();
		List<Issue> issueAuxList = new ArrayList<>();
		List<IssueData> auxIssueDataList = new ArrayList<>();

		if (issueDataList.isPresent()) {
			auxIssueDataList.addAll(issueDataList.get());
			IssueDao issueDao = new IssueDao();
			try {
				issueGitHubId = issueDao.getGitHubIssuesID(project.getIdDB());
				issueAuxList = issueDao.getIssuesFromDataBase(project.getIdDB());
				resultIssueList.addAll(issueAuxList);

			} catch (InvalidBeanException | SQLException e1) {
				e1.printStackTrace();
				Logger.log(log, "ERROR GETTING ISSUES in DATABASE");
			}

			if (!issueGitHubId.isEmpty()) {
				for (IssueData auxIssue : issueDataList.get()) {
					if (issueGitHubId.contains(auxIssue.number)) {
						auxIssueDataList.remove(auxIssue);
					}
				}
			}

			// Get the collaborators of each issue
			for (IssueData issueData : auxIssueDataList) {
				Issue newIssue = getIssueCollaborators(issueData, pullList);
				newIssue.setProjectID(project.getIdDB());
				try {
					DBWriter.INSTANCE.persistIssueDB(newIssue);
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.log(log, "ERROR PERSISTING ISSUE in DATABASE");
				}
				resultIssueList.add(newIssue);
			}

			if (!auxIssueDataList.isEmpty()) {
				try {
					DBWriter.INSTANCE.persistRelatedIssueDB(resultIssueList);
				} catch (SQLException e) {
					e.printStackTrace();
					Logger.log(log, "ERROR PERSISTING RELATED ISSUES in DATABASE");
				}
			}
			Logger.log(log, "Issue building finished.");
		}
		return resultIssueList;
	}

	/**
	 * Get the Collaborators of each Issue, as well as all other information of
	 * an Issue
	 * 
	 * @param issueData
	 * @param pullList
	 * @return an Issue
	 */
	private Issue getIssueCollaborators(IssueData issueData, Optional<List<PullRequest>> pullList) {

		String commitMergeClue = "";
		Issue newIssue = new Issue();

		newIssue.setIssueID(issueData.number);
		newIssue.setOpenedDate(issueData.created_at);
		newIssue.setClosedDate(issueData.closed_at);
		newIssue.setIsPullRequest(issueData.isPullRequest);
		if (issueData.state.equals(State.OPEN)) {
			newIssue.setIsClosed(false);
		} else {
			newIssue.setIsClosed(true);
		}

		List<String> relatedIssues = new ArrayList<>();

		DeveloperNode devOwner = new DeveloperNode();
		if (issueData.user != null) {
			devOwner.setName(issueData.user.username);
			devOwner.setEmail(issueData.user.email);
		}

		if (devOwner.getName() != null) {
			newIssue.getDevList().add(devOwner);
			newIssue.addEvent(new Event(newIssue.getIssueID(), issueData.created_at, devOwner, false));
		}

		// Checking if there is other issues linked in the body of the target
		// Issue
		if (issueData.body != null && issueData.body.contains("#")) {
			relatedIssues.addAll(getRelatedIssues(new String(issueData.body)));
		}

		// Search for developers in the list of comments
		for (CommentData comment : issueData.getCommentsList()) {

			DeveloperNode dev = new DeveloperNode();
			if (comment.user != null) {
				dev.setName(comment.user.username);
				dev.setEmail(comment.user.email);
			} else {
				continue;
			}

			// Creating an event for each GitHub commentary
			newIssue.addEvent(new Event(newIssue.getIssueID(), comment.created_at, dev, true));

			// Checking if there is other issues linked
			if (comment.body != null && comment.body.contains("#")) {
				relatedIssues.addAll(getRelatedIssues(new String(comment.body)));
			}

			if (!newIssue.getDevList().contains(dev)) {
				newIssue.getDevList().add(dev);
			}
		}

		// Adding the related issues in the target Issue
		for (String relatedIssue : relatedIssues) {
			Integer aux = Integer.parseInt(relatedIssue.replace("#", ""));
			if (!newIssue.getRelatedIssues().contains(aux)) {
				newIssue.addRelatedIssue(aux);
			}
		}

		// Search for developers in the list of events
		for (EventData event : issueData.getEventsList()) {

			DeveloperNode dev = new DeveloperNode();
			if (event.user != null) {
				dev.setName(event.user.username);
				dev.setEmail(event.user.email);
			} else {
				continue;
			}

			// Ignore this labels we are not interested
			// cite an author is not reason to include him in the communication
			// network
			if (event.event.equals("mentioned") || event.event.equals("subscribed")) {
				continue;
			} else {

				// Creating an event for each GitHub event, except for
				// 'mentioned' and
				// 'subscribed'. The reason is on the comment above.
				newIssue.addEvent(new Event(newIssue.getIssueID(), event.created_at, dev, false));

				// Getting related labels
				if (event.event.equals("labeled")) {
					newIssue.addRelatedLabel(cleanLabelName(((EventData.LabeledEventData) event).label.name));
				}

				// Getting the referenced commit that's merged commit, but it is
				// a String
				if (event.event.equals("merged")) {
					commitMergeClue = ((EventData.ReferencedEventData) event).commit_id;
				}
			}

			if (!newIssue.getDevList().contains(dev)) {
				newIssue.getDevList().add(dev);
			}
		}

		// Search for commits in the list of related commits
		for (Commit commit : issueData.getRelatedCommits()) {
			if (!newIssue.getRelatedCommits().contains(commit)) {
				newIssue.addRelatedCommit(commit);
			}

			// One commit can be the merge commit. We need that commit to link
			// it with merge scenarios
			if (commit.getId().equals(commitMergeClue) && newIssue.getIsPullRequest() && newIssue.getIsClosed()) {
				newIssue.setMergeCommit(commit);
				newIssue.setPullHash(commitMergeClue);
			}

		}

		// Adding referenced commits. GitHubWrapper does not get it as an Issue
		// that's why we need looking into pull requests
		for (PullRequest pullRequest : pullList.get()) {

			if (newIssue.getIssueID() == pullRequest.getIssue().number) {
				List<Commit> commitList = pullRequest.getCommits();
				if (!commitList.isEmpty()) {
					for (Commit commit : commitList) {
						if (!newIssue.getRelatedCommits().contains(commit)) {
							newIssue.addRelatedCommit(commit);
						}
					}
				}
			}
		}

		return newIssue;

	}

	private String cleanLabelName(String labelName) {
		char ch;
		// search for strange char in the String
		for (int i = 0; i < labelName.length(); i += 1) {
			ch = labelName.charAt(i);
			// type 19 has the strange symbols
			if (Character.getType(ch) == 19) {
				if (labelName.length() == 1) {
					labelName = "";
				} else {
					labelName = labelName.substring(0, i) + labelName.substring(i + 1);
					i--;
				}
			}
		}
		return labelName;
	}

	private ArrayList<String> getRelatedIssues(String string) {
		ArrayList<String> relatedIssues = new ArrayList<>();
		ArrayList<String> relatedIssuesRefined = new ArrayList<>();
		String[] s = {};
		String auxString = "";

		string = string.replaceAll("    .*?\n", "").replaceAll("    .*?\r", "").replaceAll("\r", "")
				.replaceAll("\n", "").replaceAll("```.*?```", "").replaceAll("``.*?``", "").replaceAll("`.*?`", "");

		Pattern pattern = Pattern.compile("#([0-9]+)");
		// URLs uses "/". So, if a word has it it will not be a link that belong
		// to the target project
		if (string.contains("/")) {
			s = string.split(" ");
		} else {
			auxString = string;
		}

		for (String i : s) {
			if (!i.contains("/")) {
				auxString = auxString + " " + i;
			}
		}

		Matcher m = pattern.matcher(auxString);
		while (m.find()) {
			relatedIssues.add(m.group());
		}

		for (String relatedIssueId : relatedIssues) {
			if (relatedIssueId.length() < 11) {
				relatedIssuesRefined.add(relatedIssueId);
			}
		}

		return relatedIssuesRefined;
	}

	public List<Issue> buildIssuesFromDatabase() throws IOException {

		List<Issue> resultIssueList = new ArrayList<Issue>();

		IssueDao issueDao = new IssueDao();
		try {
			resultIssueList = issueDao.getIssuesFromDataBase(project.getIdDB());

		} catch (InvalidBeanException | SQLException e1) {
			e1.printStackTrace();
			Logger.log(log, "ERROR GETTING ISSUES in DATABASE");
		}

		return resultIssueList;
	}

}
