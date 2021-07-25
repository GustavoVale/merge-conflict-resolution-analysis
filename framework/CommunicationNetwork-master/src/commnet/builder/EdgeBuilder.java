package commnet.builder;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import commnet.model.beans.Chunk;
import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.Event;
import commnet.model.beans.FileMS;
import commnet.model.beans.Issue;
import commnet.model.beans.MergeScenario;
import commnet.model.beans.Network;
import commnet.model.dao.FileDao;
import commnet.model.dao.IssueCommitDao;
import commnet.model.datastructures.EventBinaryTree;
import commnet.model.db.DBWriter;
import commnet.model.enums.EdgeSide;
import commnet.model.enums.NetworkType;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class EdgeBuilder implements Runnable {

	protected File log;
	private MergeScenario mergeScenario;
	private NetworkType netType;
	private EventBinaryTree ebt;
	private List<Issue> issueList;
	private HashMap<String, Integer> devHash;

	private EdgeBuilder edgeBuild;

	private IssueCommitDao issueCommitDao = new IssueCommitDao();
	private FileDao fileDao = new FileDao();

	public EdgeBuilder(HashMap<String, Integer> devHash, MergeScenario mergeScenario, EventBinaryTree ebt,
			List<Issue> issueList, NetworkType netType, File log) {
		this.setDevHash(devHash);
		this.setMergeScenario(mergeScenario);
		this.setEbt(ebt);
		this.setIssueList(issueList);
		this.setNetType(netType);
		this.setLog(log);
	}

	public HashMap<String, Integer> getDevHash() {
		return devHash;
	}

	public void setDevHash(HashMap<String, Integer> devHash) {
		this.devHash = devHash;
	}

	public File getLog() {
		return log;
	}

	public void setLog(File log) {
		this.log = log;
	}

	public MergeScenario getMergeScenario() {
		return mergeScenario;
	}

	public void setMergeScenario(MergeScenario merge) {
		this.mergeScenario = merge;
	}

	public NetworkType getNetType() {
		return netType;
	}

	public void setNetType(NetworkType netType) {
		this.netType = netType;
	}

	public EventBinaryTree getEbt() {
		return ebt;
	}

	public void setEbt(EventBinaryTree ebt) {
		this.ebt = ebt;
	}

	public List<Issue> getIssueList() {
		return issueList;
	}

	public void setIssueList(List<Issue> issueList) {
		this.issueList = issueList;
	}

	public EdgeBuilder getEdgeBuild() {
		return edgeBuild;
	}

	public void setEdgeBuild(EdgeBuilder edgeBuild) {
		this.edgeBuild = edgeBuild;
	}

	@Override
	public void run() {
		List<DeveloperEdge> contributionNetwork = new ArrayList<>();
		List<DeveloperEdge> timeRangeNetList = new ArrayList<>();
		List<DeveloperEdge> pullBasedNetList = new ArrayList<>();
		List<DeveloperEdge> changedArtifactBasedNetList = new ArrayList<>();

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.CONTRIBUTIONNETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)) {
			// Build the Contribution network
			contributionNetwork = getContributionNetwork();
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.COMMUNICATIONNETWORKS)
				|| netType.equals(NetworkType.COMPREHENSIVENETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)) {
			// Build the Communication network considering Time Ranges
			// (Awareness-based approach)
			timeRangeNetList = getTimeRangeNetwork();
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.COMMUNICATIONNETWORKS) || netType.equals(NetworkType.PRECISENETWORKS)
				|| netType.equals(NetworkType.CONTCOMNETWORKS)) {
			// Build the Communication network considering issues and related
			// issues (Pull-request-based approach)
			pullBasedNetList = getPullBasedNetwork();
		}
		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.COMMUNICATIONNETWORKS)
				|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)) {
			// Build the Communication network considering changed artifacts
			// (Changed artifact-based approach)
			changedArtifactBasedNetList = getChangedArtifactBasedNetwork();
		}

		// Merge the two lists
		timeRangeNetList.addAll(changedArtifactBasedNetList);
		timeRangeNetList.addAll(pullBasedNetList);
		contributionNetwork.addAll(timeRangeNetList);

		contributionNetwork = persistDevEdges(contributionNetwork);

		Network newNet = new Network(mergeScenario.getProjectidDB(), mergeScenario.getIdDB(), mergeScenario,
				contributionNetwork);

		try {
			DBWriter.INSTANCE.persistNetworkDB(newNet);
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(log, "ERROR PERSISTING NETWORK in DATABASE");
		}

	}

	/**
	 * Build contribution networks with all developers that change the source
	 * code in a merge scenario
	 * 
	 * @param mergeScenarioList
	 * @return a network for each merge scenario of a project
	 */
	private List<DeveloperEdge> getContributionNetwork() {

		List<DeveloperEdge> mergeEdges = new ArrayList<>();
		List<DeveloperEdge> fileEdges = new ArrayList<>();
		List<DeveloperEdge> chunkEdges = new ArrayList<>();
		mergeEdges.addAll(mergeScenario.getMergeDevEdges());

		for (FileMS file : mergeScenario.getListFileMS()) {
			fileEdges.addAll(file.getFileDevEdges());
			for (Chunk<?> chunk : file.getChunkList()) {
				chunkEdges.addAll(chunk.getDevEdges());
			}
		}

		chunkEdges.addAll(fileEdges);
		chunkEdges.addAll(mergeEdges);

		return chunkEdges;
	}

	/**
	 * Compare the edges and remove duplicates
	 * 
	 * @param edges
	 */
	private void removingDuplicateEdges(List<DeveloperEdge> edges) {
		for (int i = 0; i < edges.size(); i++) {
			DeveloperEdge edge1 = edges.get(i);
			for (int j = i + 1; j < edges.size(); j++) {
				DeveloperEdge edge2 = edges.get(j);
				if (edge1.equals(edge2)) {
					edges.get(i).setWeight(edges.get(i).getWeight() + edges.get(j).getWeight());
					edges.remove(j);
					j--;
				}
			}
		}

	}

	/**
	 * Create a communication network based on pull requests type 13 and 14
	 * 
	 * @param issueList
	 * @param mergeList
	 * @return a list of edges for each pull request and related issues
	 */
	private List<DeveloperEdge> getPullBasedNetwork() {

		List<DeveloperEdge> edges = new ArrayList<>();

		for (Issue issue : issueList) {
			// If it is a closed pull request
			if (issue.getPullHash() != null
					&& issue.getPullHash().equals(mergeScenario.getMergeCommitHash().toString())) {

				// creating edges type 13
				edges.addAll(createEdges(issue.getDevList(), 13));

				// checking if there are related issues
				if (!issue.getRelatedIssues().isEmpty()) {
					List<DeveloperNode> relatedDevList = new ArrayList<>();
					for (Integer relatedIssueID : issue.getRelatedIssues()) {
						for (Issue comu2 : issueList) {
							if (comu2.getIssueID() == relatedIssueID) {
								List<DeveloperNode> auxDevList = comu2.getDevList();
								for (DeveloperNode node : auxDevList) {
									if (!relatedDevList.contains(node)) {
										relatedDevList.add(node);
									}
								}
							}
						}
					}
					// creating edges type 14 and adding to the list of edges
					edges.addAll(createRelatedEdges(issue, relatedDevList, edges));
				}

				if (!edges.isEmpty()) {
					removingDuplicateEdges(edges);
				}
			}
		}

		return edges;
	}

	/**
	 * Create a full graph of developerEdges for a pre-defined type and issue
	 * 
	 * @param nodes
	 * @param type
	 * @param newIssueID
	 * @return list of edges
	 */
	private List<DeveloperEdge> createEdges(List<DeveloperNode> nodes, Integer type) {

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

	/**
	 * Create edges related for a issue related to a pull request (type 14)
	 * 
	 * @param newIssue
	 * @param relatedDevList
	 * @param edges
	 * @return List of Edges
	 */
	private List<DeveloperEdge> createRelatedEdges(Issue newIssue, List<DeveloperNode> relatedDevList,
			List<DeveloperEdge> edges) {

		for (DeveloperNode devA : relatedDevList) {
			if (!newIssue.getDevList().contains(devA)) {
				for (DeveloperNode devCommuNet : newIssue.getDevList()) {
					if (!devA.equals(devCommuNet)) {
						edges.add(new DeveloperEdge(devCommuNet, devA, 14, EdgeSide.BOTH, 1));
					}
				}
				newIssue.addDev(devA);
			} else {
				edges.add(new DeveloperEdge(devA, devA, 14, EdgeSide.BOTH, 1));
			}
		}

		return edges;
	}

	/**
	 * Build TimeRangeNetwork for all developers that were active in a merge
	 * scenario
	 * 
	 * @param issueList
	 * @param mergeList
	 * @return a list of developerEdge for each pull request
	 */
	private List<DeveloperEdge> getTimeRangeNetwork() {

		List<DeveloperEdge> timeRangeEdges = new ArrayList<>();
		List<Event> timeRangeEventsList = new ArrayList<Event>();

		// creates a list with all events that happen in the MS time range
		timeRangeEventsList = ebt.values(mergeScenario.getBaseDate(), mergeScenario.getMergeDate()).get();

		if (mergeScenario != null) {

			// Get developers that communicate when a merge scenario was
			// opened
			List<DeveloperNode> devByEventsList = getDevTimeRange(timeRangeEventsList, mergeScenario);

			// Build edges of developers that communicate when a merge
			// scenario was opened **TYPE 15**
			List<DeveloperEdge> edgeByEventsList = createEdges(devByEventsList, 15);
			if (!edgeByEventsList.isEmpty()) {
				timeRangeEdges.addAll(edgeByEventsList);
			}

			// Get developers from issues in the time range of the merge
			// scenario
			// (contribution) that communicate before the time range
			List<DeveloperNode> devByIssueList = getDevIssueTimeRange(issueList, timeRangeEventsList, mergeScenario);

			// Build edges of developers that communicate on Issues when a
			// merge scenario was opened, but before the timeRange **TYPE 16**
			List<DeveloperEdge> edgeListBeforeTimerange = getDevEdgeFromIssuesInTimeRange(devByEventsList,
					devByIssueList);
			if (!edgeListBeforeTimerange.isEmpty()) {
				timeRangeEdges.addAll(edgeListBeforeTimerange);
			}

		}

		return timeRangeEdges;
	}

	/**
	 * Get developers that create an event while a merge scenario was opened
	 * 
	 * @param eventsList
	 * @param mergeScenario
	 * @return list of develooerNodes
	 */
	private List<DeveloperNode> getDevTimeRange(List<Event> eventsList, MergeScenario mergeScenario) {

		List<DeveloperNode> devByEventsList = new ArrayList<DeveloperNode>();

		for (Event event : eventsList) {
			if (!devByEventsList.contains(event.getCont())) {
				devByEventsList.add(event.getCont());
			}
		}
		return devByEventsList;
	}

	/**
	 * Get a list of developerNodes that create an event in an issue when a
	 * merge scenario was opened
	 * 
	 * @param eventsList
	 * @param merge
	 * @return list of developerNodes
	 */
	private List<DeveloperNode> getDevIssueTimeRange(List<Issue> issuesList, List<Event> timeRangeEventsList,
			MergeScenario merge) {

		List<Integer> issueIDList = new ArrayList<>();
		List<DeveloperNode> devByEventsList = new ArrayList<DeveloperNode>();

		for (Event event : timeRangeEventsList) {
			if (!issueIDList.contains(event.getIssueID())) {
				issueIDList.add(event.getIssueID());
			}
		}

		for (Issue issue : issuesList) {
			for (Event event : issue.getRelatedEvents()) {
				if (issueIDList.contains(event.getIssueID())
						&& event.getCreatedAt().getTime() < merge.getBaseDate().getTime()) {
					if (!devByEventsList.contains(event.getCont())) {
						devByEventsList.add(event.getCont());
					}
				}
			}
		}

		return devByEventsList;
	}

	/**
	 * Get Edges of DeveloperNodes that participated of issues that were opened
	 * during a merge scenario, but contribute to the issue before the merge
	 * scenario is opened
	 * 
	 * @param devByEventsList
	 * @param devByIssueList
	 * @return
	 */
	private List<DeveloperEdge> getDevEdgeFromIssuesInTimeRange(List<DeveloperNode> devByEventsList,
			List<DeveloperNode> devByIssueList) {
		List<DeveloperEdge> edgeList = new ArrayList<DeveloperEdge>();
		for (DeveloperNode devA : devByEventsList) {
			if (devByIssueList.contains(devA)) {
				edgeList.add(new DeveloperEdge(devA, devA, 16, EdgeSide.BOTH, 1));
				devByIssueList.remove(devA);
			}
		}
		for (DeveloperNode devA : devByEventsList) {
			for (DeveloperNode devB : devByIssueList) {
				edgeList.add(new DeveloperEdge(devA, devB, 16, EdgeSide.BOTH, 1));
			}
		}

		return edgeList;
	}

	private List<DeveloperEdge> persistDevEdges(List<DeveloperEdge> contributionNetwork) {

		for (DeveloperEdge edge : contributionNetwork) {
			if (edge.getDevA().getIdDB() == null) {
				if (getDevHash().containsKey(edge.getDevA().getName().toLowerCase())) {
					edge.getDevA().setIdDB(getDevHash().get(edge.getDevA().getName().toLowerCase()));
				}

			}
			if (edge.getDevB().getIdDB() == null) {
				if (getDevHash().containsKey(edge.getDevB().getName().toLowerCase())) {
					edge.getDevB().setIdDB(getDevHash().get(edge.getDevB().getName().toLowerCase()));
				}

			}

		}
		return contributionNetwork;
	}

	/**
	 * Create a communication network based on changed artifacts type 17
	 * 
	 * @return a list of edges for each merge scenario for this approach
	 */
	private List<DeveloperEdge> getChangedArtifactBasedNetwork() {

		List<Event> eventsList = new ArrayList<Event>();
		List<String> filesChangedInTheMergeScenarioList = new ArrayList<String>();
		List<DeveloperEdge> changedArtifactBasedNet = new ArrayList<DeveloperEdge>();

		if (mergeScenario != null) {

			// Get a list of file paths (names) of changed in a merge scenario
			try {
				filesChangedInTheMergeScenarioList = fileDao.getListOfFilesPathsInMS(mergeScenario.getIdDB());
			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
				Logger.log(log, "ERROR RETRIEVING LIST OF FILES OF MERGE SCENARIO ID " + mergeScenario.getIdDB()
						+ " in DATABASE");
			}

			// creates a list with all events that happen in the MS time range
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
			changedArtifactBasedNet = createEdges(devList, 17);

		}
		return changedArtifactBasedNet;
	}

	/**
	 * Get issues where events in the merge scenario time range happened
	 * 
	 * @param eventsList
	 * @return a list of issue ids
	 */
	private List<Integer> getIssuesWhereEventsHappened(List<Event> eventsList) {
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
	private HashMap<Integer, ArrayList<Integer>> getIssueCommitIdListHash(List<Integer> issueIdList) {
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
	private List<Integer> getIssueListWithArtifactsChangedInTheMergeScenario(
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
	private List<DeveloperNode> getDevListLinkedWithChangedArtifactApproach(List<Integer> refinedIssueList) {
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

	private Date addOneDayInDate(Date msDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(msDate);
		c.add(Calendar.DAY_OF_MONTH, 1);

		java.util.Date newMsDate = c.getTime();
		Date newDate = new java.sql.Date(newMsDate.getTime());

		System.out.println(newDate.toString());

		return newDate;
	}
}
