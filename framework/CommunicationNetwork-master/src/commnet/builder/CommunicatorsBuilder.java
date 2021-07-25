package commnet.builder;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import commnet.model.beans.Network;
import commnet.model.dao.DeveloperEdgeDao;
import commnet.model.dao.DeveloperRoleDao;
import commnet.model.dao.NetworkDao;
import commnet.model.enums.NetworkType;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class CommunicatorsBuilder {

	private Integer projectIdDB;
	private NetworkType netType;

	private Integer approach = 0;
	private String query = "";

	private List<Integer> communicatorEdgesType = new ArrayList<>();
	private List<Integer> devEdgesType = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6));
	private List<Integer> communicatorsIdList = null;
	private List<Integer> developersIdList = null;

	protected File log;

	public CommunicatorsBuilder(Integer projectIdDB, NetworkType netType, File log) {
		setLog(log);
		setProjectIdDB(projectIdDB);
		setNetType(netType);
		findApproachType();
	}

	public Integer getProjectIdDB() {
		return projectIdDB;
	}

	public void setProjectIdDB(Integer projectIdDB) {
		this.projectIdDB = projectIdDB;
	}

	public NetworkType getNetType() {
		return netType;
	}

	public void setNetType(NetworkType netType) {
		this.netType = netType;
	}

	public File getLog() {
		return log;
	}

	public void setLog(File log) {
		this.log = log;
	}

	private void findApproachType() {
		if (netType.equals(NetworkType.COMPREHENSIVECOMMUNICATORS)) {
			approach = 1;
			communicatorEdgesType.add(13);
			communicatorEdgesType.add(14);

		} else if (netType.equals(NetworkType.PRECISECOMMUNICATORS)) {
			approach = 2;
			communicatorEdgesType.add(15);
			communicatorEdgesType.add(16);
		} else if (netType.equals(NetworkType.CHANGEDARTIFACTCOMMUNICATORS)) {
			approach = 3;
			communicatorEdgesType.add(17);
		}
		
	}

	public void builder() {
		try {
			NetworkDao ndao = new NetworkDao();
			List<Network> netList = ndao.getNetworkListByProject(projectIdDB);

			// get merge scenario ids which there is communicators_conts and
			// communicatiors_devs already stored into the database
			List<Integer> communicatorsContsMsIdDb = getDevRoleMergeScenarioList(netList, "communicators_conts");
			List<Integer> communicatorsDevsMsIdDb = getDevRoleMergeScenarioList(netList, "communicators_devs");
			List<Integer> union = getUnionOfLists(communicatorsContsMsIdDb, communicatorsDevsMsIdDb);

			// for each network of a project save communicators
			for (Network net : netList) {

				// set just for the ones that it is not stored into the
				// database
				if (!union.contains(net.getmsIdDB())) {
					communicatorsIdList = getListOfContributorsInNetworkRefinedByEdgeType(net, communicatorEdgesType);
					developersIdList = getListOfContributorsInNetworkRefinedByEdgeType(net, devEdgesType);
					developersIdList = RemainJustDeveloperIdsThatCommunicate();

					for (Integer devId : communicatorsIdList) {
						buildQuery("communicators_conts", net.getmsIdDB(), devId);

						if (query.length() > 65475) {
							savingRowsInDatabase();
						}
					}
					if (!query.isEmpty()) {
						savingRowsInDatabase();
					}

					for (Integer devId : developersIdList) {
						buildQuery("communicators_devs", net.getmsIdDB(), devId);

						if (query.length() > 65475) {
							savingRowsInDatabase();
						}
					}
					if (!query.isEmpty()) {
						savingRowsInDatabase();
					}
				}

			}

		} catch (InvalidBeanException | SQLException e) {
			Logger.log(log, "ERROR GETTING COMMUNICATORS 1.2");
			e.printStackTrace();
		}

	}

	private List<Integer> getDevRoleMergeScenarioList(List<Network> netList, String tableName) {

		List<Integer> devRoleIdList = new ArrayList<>();
		String newQuery = "";

		List<Integer> devRoleMsIdList = getDevRoleMergeScenarioIdList(netList);

		for (Integer devRoleMsId : devRoleMsIdList) {
			newQuery = creatingNewQuery(newQuery, devRoleMsId, tableName);

			if (newQuery.length() > 65475) {
				devRoleIdList.addAll(getDevRoleIdList(newQuery));
				newQuery = "";
			}
		}
		if (!newQuery.isEmpty()) {
			devRoleIdList.addAll(getDevRoleIdList(newQuery));
		}
		return devRoleIdList;
	}

	private List<Integer> getDevRoleMergeScenarioIdList(List<Network> netList) {
		List<Integer> result = new ArrayList<>();
		for (Network net : netList) {
			result.add(net.getmsIdDB());
		}
		return result;
	}

	private String creatingNewQuery(String newQuery, Integer devRoleMsId, String tableName) {

		if (newQuery.isEmpty()) {
			newQuery = ("select distinct(merge_scenario_id) from " + tableName + "  where approach=" + approach
					+ " AND (");
		}
		newQuery = newQuery + getNewQueryRow(devRoleMsId);
		return newQuery;
	}

	private String getNewQueryRow(Integer devRoleMsId) {

		String result = "`merge_scenario_id`=" + devRoleMsId + " OR ";
		return result;
	}

	private List<Integer> getDevRoleIdList(String newQuery) {
		List<Integer> devRoleIdList = null;

		DeveloperRoleDao devRoleDao = new DeveloperRoleDao();
		newQuery = newQuery.substring(0, newQuery.length() - 4);
		newQuery = newQuery + ");";

		try {
			devRoleIdList = devRoleDao.getMergeScenarioIdListByQueryMade(newQuery);
		} catch (InvalidBeanException | SQLException e) {
			Logger.log(log, "ERROR SAVING COMMUNICATORS INTO THE DATABASE, when creating query");
			e.printStackTrace();
		}
		return devRoleIdList;
	}

	private List<Integer> RemainJustDeveloperIdsThatCommunicate() {
		List<Integer> result = new ArrayList<>();
		for (Integer devId : communicatorsIdList) {
			if (developersIdList.contains(devId)) {
				result.add(devId);
			}
		}
		return result;
	}

	private List<Integer> getListOfContributorsInNetworkRefinedByEdgeType(Network net, List<Integer> edgeTypeList) {

		DeveloperEdgeDao edgeDao = new DeveloperEdgeDao();
		List<Integer> devAList = new ArrayList<>();
		try {
			devAList = edgeDao.getDistinctDevByNetworkId("dev_a", net.getIdDB(), edgeTypeList);
			List<Integer> devBList = edgeDao.getDistinctDevByNetworkId("dev_b", net.getIdDB(), edgeTypeList);

			for (Integer devId : devBList) {
				if (!devAList.contains(devId)) {
					devAList.add(devId);
				}
			}

		} catch (InvalidBeanException | SQLException e) {
			Logger.log(log, "ERROR GETTING COMMUNICATORS, problem in getDistinctDevByNetworkId method");
			e.printStackTrace();
		}

		return devAList;
	}

	private void buildQuery(String tableName, Integer msId, Integer devId) {
		if (query.isEmpty()) {
			query = ("insert into `" + tableName
					+ "` (`merge_scenario_id`, `contributor_id`, `approach`) values");
		}
		query = query + getNewRow(msId, devId);
	}

	private String getNewRow(Integer msId, Integer devId) {
		String space = ", ";
		String result = " (" + msId + space + devId + space + approach + "),";

		return result;
	}

	private void savingRowsInDatabase() throws InvalidBeanException, SQLException {
		DeveloperRoleDao devRoleDao = new DeveloperRoleDao();
		query = query.substring(0, query.length() - 1);
		query = query + ";";

		if (!devRoleDao.saveManyRows(query)) {
			Logger.log(log, "ERROR SAVING COMMUNICATORS, QUERY WRONG");
			throw new RuntimeException("Communicators_conts OR Communicators_devs could not be stored to database!");
		}
		query = "";
	}
	
	private List<Integer> getUnionOfLists(List<Integer> communicatorsContsMsIdDb,
			List<Integer> communicatorsDevsMsIdDb) {
		Set<Integer> set = new HashSet<>();
		set.addAll(communicatorsDevsMsIdDb);
		set.addAll(communicatorsContsMsIdDb);
		return new ArrayList<Integer>(set);
	}

}
