package commnet.test;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.Event;
import commnet.model.beans.Issue;
import commnet.model.beans.MergeScenario;
import commnet.model.dao.FileDao;
import commnet.model.dao.IssueCommitDao;
import commnet.model.dao.IssueDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.datastructures.EventBinaryTree;
import commnet.model.enums.EdgeSide;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ChangedArtifactBasedNetworkBuilderTest {

	protected static File log;
	private static MergeScenario mergeScenario;

	private static List<MergeScenario> msList;

	// private static EventBinaryTree ebt;
	private static List<Issue> issueList;

	private static IssueCommitDao issueCommitDao = new IssueCommitDao();
	private static FileDao fileDao = new FileDao();
	private static MergeScenarioDao msDao = new MergeScenarioDao();
	private static IssueDao issueDao = new IssueDao();

	public static void main(String[] args) {
		HashMap<Integer, ArrayList<DeveloperEdge>> developerEdgeList = new HashMap<Integer, ArrayList<DeveloperEdge>>();
		EventBinaryTree ebt = new EventBinaryTree();
		String chunkHasConflict = "";
		try {
			msList = msDao.getMergeScenariosFromDataBase(38, chunkHasConflict);
			issueList = issueDao.getIssuesFromDataBase(38);

			for (Issue issue : issueList) {
				System.out.println(issue.getRelatedEvents().size());
				ebt.add(issue.getRelatedEvents());
			}

			for (MergeScenario ms : msList) {
				mergeScenario = ms;
				developerEdgeList.put(ms.getIdDB(), getChangedArtifactBasedNetwork(ebt));
			}

			System.out.println("I am here " + developerEdgeList.size());

		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a communication network based on changed artifacts type 17
	 * 
	 * @return a list of edges for each merge scenario for this approach
	 */
	private static ArrayList<DeveloperEdge> getChangedArtifactBasedNetwork(EventBinaryTree ebt) {

		List<Event> eventsList = new ArrayList<Event>();
		List<String> filesChangedInTheMergeScenarioList = new ArrayList<String>();
		ArrayList<DeveloperEdge> changedArtifactBasedNet = new ArrayList<DeveloperEdge>();

		if (mergeScenario != null) {

			// Get a list of file paths (names) of changed in a merge scenario
			try {
				filesChangedInTheMergeScenarioList = fileDao.getListOfFilesPathsInMS(mergeScenario.getIdDB());
			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
				Logger.log(log, "ERROR RETRIEVING LIST OF FILES OF MERGE SCENARIO ID " + mergeScenario.getIdDB()
						+ " in DATABASE");
			}

			// Create a list with all events that happen in the MS time range
			eventsList = ebt.values(mergeScenario.getBaseDate(), mergeScenario.getMergeDate()).get();

			// Get the issues where the events happened
			List<Integer> issueIdList = getIssuesWhereEventsHappened(eventsList);

			// Create a HashMap with issue ids (key) and a list of commit ids
			// (values) related to each issue id
			HashMap<Integer, ArrayList<Integer>> issueIdCommitIdListHash = getIssueCommitIdListHash(issueIdList);

			// Get just issues that have commits related to the code changes in
			// the merge scenario
			List<Integer> refinedIssueList = getIssueListWithArtifactsChangedInTheMergeScenario(issueIdCommitIdListHash,
					filesChangedInTheMergeScenarioList);

			// Get developers that contribute to issues in which are linked with
			// code changes of the merge scenario
			List<DeveloperNode> devList = getDevListLinkedWithChangedArtifactApproach(refinedIssueList);

			// Build developer edges of **TYPE 17**
			changedArtifactBasedNet = (ArrayList<DeveloperEdge>) createEdges(devList, 17);

		}
		return changedArtifactBasedNet;
	}

	/**
	 * Get issues where events in the merge scenario time range happened
	 * 
	 * @param eventsList
	 * @return a list of issue ids
	 */
	private static List<Integer> getIssuesWhereEventsHappened(List<Event> eventsList) {
		List<Integer> issueIdList = new ArrayList<>();
		for (Event event : eventsList) {
			if (!issueIdList.contains(event.getIssueIdDB())) {
				issueIdList.add(event.getIssueIdDB());
			}
		}
		return issueIdList;
	}

	/**
	 * Create a HashMap with issue ids (key) and a list of commit ids (values)
	 * related to each issue id
	 * 
	 * @param issueIdList
	 * @return a HashMap
	 */
	private static HashMap<Integer, ArrayList<Integer>> getIssueCommitIdListHash(List<Integer> issueIdList) {
		HashMap<Integer, ArrayList<Integer>> issueIdCommitIdListHash = new HashMap<Integer, ArrayList<Integer>>();

		for (Integer issueId : issueIdList) {
			try {
				issueIdCommitIdListHash.put(issueId, issueCommitDao.getCommitIdListByIssue(issueId));
			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
				Logger.log(log, "ERROR RETRIEVING LIST OF COMMIT IDS OF ISSUE " + issueId + " in DATABASE");
			}
		}
		return issueIdCommitIdListHash;
	}

	/**
	 * Check the files changed of each commit related to the issues and compare
	 * if they were changed in the merge scenario
	 * 
	 * @param issueIdCommitIdListHash
	 * @param filesChangedInTheMergeScenarioList
	 * @return a list of issue ids
	 */
	private static List<Integer> getIssueListWithArtifactsChangedInTheMergeScenario(
			HashMap<Integer, ArrayList<Integer>> issueIdCommitIdListHash,
			List<String> filesChangedInTheMergeScenarioList) {
		List<Integer> refinedIssueList = new ArrayList<>();

		for (Entry<Integer, ArrayList<Integer>> entry : issueIdCommitIdListHash.entrySet()) {
			outerloop: {
				for (Integer commitId : entry.getValue()) {

					try {
						if (commitId == mergeScenario.getMergeCommitIdDB()
								&& !refinedIssueList.contains(entry.getKey())) {
							refinedIssueList.add(entry.getKey());
							break outerloop;
						} else {
							List<String> filesChangedList = fileDao.getListOfFilesPathsChangedByCommitId(commitId);

							for (String filepath : filesChangedList) {
								if (filesChangedInTheMergeScenarioList.contains(filepath.toString())
										&& !refinedIssueList.contains(entry.getKey())) {
									refinedIssueList.add(entry.getKey());
									break outerloop;
								}
							}
						}

					} catch (InvalidBeanException | SQLException e) {
						e.printStackTrace();
						Logger.log(log, "ERROR RETRIEVING LIST OF FILE PATHS OF COMMIT " + commitId + " in DATABASE");
					}
				}
			}
		}
		return refinedIssueList;
	}
	
	/**
	 * Get developers that create events in the remaining issues and before the
	 * merge
	 * 
	 * @param refinedIssueList
	 * @return a list of developerNode
	 */
	private static List<DeveloperNode> getDevListLinkedWithChangedArtifactApproach(List<Integer> refinedIssueList) {
		List<DeveloperNode> devList = new ArrayList<DeveloperNode>();
		// add one as we miss the hours and minutes to avoid events created in
		// the same day
		Date msDate = addOneDayInDate(mergeScenario.getMergeDate());
		for (Issue issue : issueList) {
			if (refinedIssueList.contains(issue.getIdDB())) {
				for (Event event : issue.getRelatedEvents()) {
					if (!event.getCreatedAt().after(msDate) && !devList.contains(event.getCont())) {
						devList.add(event.getCont());
					}
				}
			}
		}
		return devList;
	}

	private static Date addOneDayInDate(Date msDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(msDate);
		c.add(Calendar.DAY_OF_MONTH, 1);

		java.util.Date newMsDate = c.getTime();
		Date newDate = new java.sql.Date(newMsDate.getTime());

		System.out.println(newDate.toString());

		return newDate;
	}

	/**
	 * Create a full graph of developerEdges for a pre-defined type and issue
	 * 
	 * @param nodes
	 * @param type
	 * @return list of edges
	 */
	private static List<DeveloperEdge> createEdges(List<DeveloperNode> nodes, Integer type) {

		List<DeveloperEdge> edges = new ArrayList<>();

		if (nodes.size() == 1) {
			edges.add(new DeveloperEdge(nodes.get(0), nodes.get(0), type, EdgeSide.BOTH, 1));
		} else if (nodes.size() == 2 && nodes.get(0).equals(nodes.get(1))) {
			if (!nodes.get(0).getEmail().equals("")) {
				edges.add(new DeveloperEdge(nodes.get(0), nodes.get(0), type, EdgeSide.BOTH, 1));
			} else {
				edges.add(new DeveloperEdge(nodes.get(1), nodes.get(1), type, EdgeSide.BOTH, 1));
			}
		} else {
			for (DeveloperNode devA : nodes) {
				for (DeveloperNode devB : nodes) {
					if (devA.equals(devB)) {
						continue;
					}
					DeveloperEdge newEdge = new DeveloperEdge(devA, devB, type, EdgeSide.BOTH, 1);
					if (!edges.contains(newEdge)) {
						edges.add(newEdge);
					}
				}
			}
		}
		return edges;

	}
}
