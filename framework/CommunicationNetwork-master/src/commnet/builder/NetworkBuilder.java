package commnet.builder;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import commnet.crawler.NCThreadPoolExecutor;
import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.Issue;
import commnet.model.beans.MergeScenario;
import commnet.model.beans.Network;
import commnet.model.beans.Project;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.NetworkDao;
import commnet.model.datastructures.EventBinaryTree;
import commnet.model.enums.NetworkType;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class NetworkBuilder {

	protected Project project;
	protected File log;
	private List<MergeScenario> mergeList;
	private List<Issue> issueList;
	private NetworkType netType;
	private EventBinaryTree ebt;

	private HashMap<String, Integer> devHashMap;

	public NetworkBuilder(Project project, List<MergeScenario> mergeScenarioList, List<Issue> issueList,
			NetworkType netType) {
		this(project, mergeScenarioList, issueList, netType, new EventBinaryTree());
	}

	public NetworkBuilder(Project project, List<MergeScenario> mergeScenarioList, List<Issue> issueList,
			NetworkType netType, EventBinaryTree ebt) {
		setProject(project);
		setMergeList(mergeScenarioList);
		setIssueList(issueList);
		setNetType(netType);
		setEbt(ebt);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public File getLog() {
		return log;
	}

	public void setLogFile(File log) {
		this.log = log;
	}

	public List<MergeScenario> getMergeList() {
		return mergeList;
	}

	public void setMergeList(List<MergeScenario> mergeList) {
		this.mergeList = mergeList;
	}

	public List<Issue> getIssueList() {
		return issueList;
	}

	public void setIssueList(List<Issue> issueList) {
		this.issueList = issueList;
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

	/**
	 * Build the networks
	 */
	public void build() {

		List<MergeScenario> listOfMSsToCompute = new ArrayList<>();
		listOfMSsToCompute.addAll(getMergeList());

		try {
			NetworkDao netDao = new NetworkDao();

			List<Integer> listOfMSIdsAlreadyComputed = new ArrayList<>();
			List<Integer> networkTypes = new ArrayList<>();

			if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)) {
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			} else if (netType.equals(NetworkType.COMPREHENSIVENETWORKS)) {
				networkTypes.add(15);
				networkTypes.add(16);
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			} else if (netType.equals(NetworkType.PRECISENETWORKS)) {
				networkTypes.add(13);
				networkTypes.add(14);
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			} else if (netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)) {
				networkTypes.add(17);
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			} else if (netType.equals(NetworkType.COMMUNICATIONNETWORKS)) {
				networkTypes.add(13);
				networkTypes.add(14);
				networkTypes.add(15);
				networkTypes.add(16);
				networkTypes.add(17);
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			} else if (netType.equals(NetworkType.CONTRIBUTIONNETWORKS)) {
				networkTypes.add(1);
				networkTypes.add(2);
				networkTypes.add(3);
				networkTypes.add(4);
				networkTypes.add(5);
				networkTypes.add(6);
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			} else if (netType.equals(NetworkType.CONTCOMNETWORKS)) {
				networkTypes.add(1);
				networkTypes.add(2);
				networkTypes.add(3);
				networkTypes.add(4);
				networkTypes.add(5);
				networkTypes.add(6);
				networkTypes.add(15);
				networkTypes.add(16);
				listOfMSIdsAlreadyComputed = netDao.getMSIdForNetworkComputedByProject(project.getIdDB(), networkTypes);
			}

			for (MergeScenario currentMS : getMergeList()) {
				if (listOfMSIdsAlreadyComputed.contains(currentMS.getIdDB())) {
					listOfMSsToCompute.remove(currentMS);
				}
			}

			// create a list of developers by project
			DeveloperNodeDao devDao = new DeveloperNodeDao();
			devHashMap = devDao.getListDevByProject(getProject().getIdDB());

		} catch (SQLException | InvalidBeanException e1) {
			e1.printStackTrace();
			Logger.log(log, "ERROR GETTING MERGE COMMITS in DATABASE");
		}

		Logger.log(log, "Network building started.");

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.COMPREHENSIVENETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)
				|| netType.equals(NetworkType.COMMUNICATIONNETWORKS) || netType.equals(NetworkType.PRECISENETWORKS)
				|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)) {
			// creates a tree of all events ordered by date
			for (Issue issue : issueList) {
				ebt.add(issue.getRelatedEvents());
			}
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.CONTRIBUTIONNETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)) {
			ContributionEdgesBuilder contEdgesBuilder = new ContributionEdgesBuilder(listOfMSsToCompute);
			listOfMSsToCompute = contEdgesBuilder.getMergeScenariosList();
		}

		NCThreadPoolExecutor pool = new NCThreadPoolExecutor();
		for (MergeScenario merge : listOfMSsToCompute) {
			pool.runTask(new EdgeBuilder(devHashMap, merge, getEbt(), issueList, netType, getLog()));
		}

		pool.shutDown();

		Logger.log(log, " Merging Networks finished.");
	}

	/**
	 * Creates a <code>Calendar</code> object at midnight of a given
	 * <code>Date</code> instance.
	 * <p>
	 * This method is useful to unify all <code>Event</code> objects created at
	 * a given date in a set of events of that day.
	 *
	 * @param d
	 * @return a <code>Calendar</code> at midnight
	 */
	public Calendar getMidnightCalendar(Date d) {
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		// reset hour, minutes, seconds and millis
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	/**
	 * Print edges of a network (mergeScenario)
	 */
	public void print() {
		List<Network> networks = getProject().getNetworkList();
		Integer countEdges = 0;
		for (Network network : networks) {
			System.out.println("NetworkID: " + network.getIdDB());
			for (DeveloperEdge devEdge : network.getDevEdges()) {
				System.out.println("ID: " + devEdge.getDevA().getIdDB() + " - " + devEdge.getDevA().getName() + " ("
						+ devEdge.getDevA().getEmail() + ")" + " === " + "ID: " + devEdge.getDevB().getIdDB() + " - "
						+ devEdge.getDevB().getName() + " (" + devEdge.getDevA().getEmail() + ")" + " EdgeType: "
						+ devEdge.getEdgeType() + " edgeSide : " + devEdge.getEdgeSide() + " Weight: "
						+ devEdge.getWeight());
				countEdges++;
			}
		}
		System.out.println(countEdges);
	}

}
