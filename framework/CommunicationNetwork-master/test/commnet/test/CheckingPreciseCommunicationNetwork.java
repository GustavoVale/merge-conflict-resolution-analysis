package commnet.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.Issue;
import commnet.model.beans.MergeScenario;
import commnet.model.beans.Project;
import commnet.model.dao.IssueDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.enums.EdgeSide;
import commnet.model.exceptions.InvalidBeanException;

public class CheckingPreciseCommunicationNetwork {

	public static void main(String[] args) throws InvalidBeanException, SQLException, IOException {

		Project project = new Project("https://github.com/daneden/animate.css");

		try {
			DBWriter.INSTANCE.persistProject(project);
		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
			// Logger.log(log, "ERROR PERSISTING PROJECT in DATABASE");
		}

		IssueDao issueDao = new IssueDao();
		List<Issue> resultIssueList = issueDao.getIssuesFromDataBase(project.getIdDB());

		List<MergeScenario> resultMSList = buildMergeScenariosFromDatabase();

		for (MergeScenario merge : resultMSList) {
			// Network contributionNetwork = getContributionNetwork(merge);
			List<DeveloperEdge> pullBasedNetList = new ArrayList<>();
			pullBasedNetList = getPullBasedNetwork(resultIssueList, merge);

		}
	}

	public static List<MergeScenario> buildMergeScenariosFromDatabase() throws IOException {

		List<MergeScenario> result = new ArrayList<MergeScenario>();
		try {
			MergeScenarioDao mergeDao = new MergeScenarioDao();
			String chunkHasConflict = "";
			result = mergeDao.getMergeScenariosFromDataBase(5, chunkHasConflict);
		} catch (SQLException | InvalidBeanException e1) {
			e1.printStackTrace();
			// Logger.log(log, "ERROR GETTING MERGE COMMITS in DATABASE");
		}

		return result;
	}

	private static List<DeveloperEdge> getPullBasedNetwork(List<Issue> issueList, MergeScenario merge) {

		List<DeveloperEdge> edges = new ArrayList<>();

		for (Issue issue : issueList) {
			// If it is a closed pull request

			System.out.println(issue.getPullHash() + "and ms hash" + merge.getMergeCommitHash());
			if (issue.getPullHash() != null && issue.getPullHash().equals(merge.getMergeCommitHash().toString())) {

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

	private static List<DeveloperEdge> createRelatedEdges(Issue newIssue, List<DeveloperNode> relatedDevList,
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

	private static void removingDuplicateEdges(List<DeveloperEdge> edges) {
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
}
