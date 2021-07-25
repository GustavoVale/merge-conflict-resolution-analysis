package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.DeveloperRole;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class DeveloperRoleDao {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	public boolean save(DeveloperRole committer, String tableName) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into " + tableName + " ( `merge_scenario_id`, `contributor_id`) values (?,?);");
			ps.setInt(1, committer.getMergeScenarioIdDB());
			ps.setInt(2, committer.getContributorIdDB());
			hasSaved = ps.executeUpdate() > 0;
			conn.commit();
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(e);
			}

		} finally {
			closeResources();
		}
		return hasSaved;
	}

	public DeveloperRole getDeveloper(DeveloperRole devRole, String tableName)
			throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"select * from " + tableName + "  where `id`=? or (`merge_scenario_id`=? and `contributor_id`=?);");

			if (devRole.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, devRole.getIdDB());
			}
			ps.setInt(2, devRole.getMergeScenarioIdDB());
			ps.setInt(3, devRole.getContributorIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer msID = rs.getInt("merge_scenario_id");
				Integer contributorID = rs.getInt("contributor_id");
				return new DeveloperRole(idDB, msID, contributorID);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public DeveloperRole getCommunicator(DeveloperRole devRole, String tableName)
			throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from " + tableName
					+ "  where `id`=? or `merge_scenario_id`=? and `contributor_id`=? AND `is_comprehensive`;");

			if (devRole.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, devRole.getIdDB());
			}
			ps.setInt(2, devRole.getMergeScenarioIdDB());
			ps.setInt(3, devRole.getContributorIdDB());
			ps.setInt(4, devRole.getContributorIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer msID = rs.getInt("merge_scenario_id");
				Integer contributorID = rs.getInt("contributor_id");
				return new DeveloperRole(idDB, msID, contributorID);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();

	}

	public boolean saveManyRows(String query) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(query);
			hasSaved = ps.executeUpdate() > 0;
			conn.commit();
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return hasSaved;
	}

	public List<Integer> getMergeScenarioIdListByQueryMade(String query) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt("merge_scenario_id"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<DeveloperRole> getDeveloperRoleListByProjectId(Integer projectIdDB, String tableName)
			throws InvalidBeanException, SQLException {
		List<DeveloperRole> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select contributor_id, merge_scenario_id from " + tableName
					+ " inner join merge_scenarios on " + tableName
					+ ".merge_scenario_id=merge_scenarios.id where merge_scenarios.project_id=" + projectIdDB + ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(new DeveloperRole(null, rs.getInt("merge_scenario_id"), rs.getInt("contributor_id")));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public boolean saveManyRowsFromOneListOfDevRole(List<DeveloperRole> devRoleList, String tableName) {
		boolean isSaved = false;
		try {
			String devRoleQuery = "";
			for (DeveloperRole devRole : devRoleList) {

				devRoleQuery = gettingLargerQueryForDevRole(devRole, tableName, devRoleQuery);

				if (devRoleQuery.length() > 65400) {
					devRoleQuery = devRoleQuery.substring(0, devRoleQuery.length() - 1);
					devRoleQuery = devRoleQuery + ";";
					saveManyRows(devRoleQuery);
					devRoleQuery = "";
				}
			}

			if (!devRoleQuery.isEmpty()) {
				devRoleQuery = devRoleQuery.substring(0, devRoleQuery.length() - 1);
				devRoleQuery = devRoleQuery + ";";
				saveManyRows(devRoleQuery);
				isSaved = true;
				devRoleQuery = "";
			}
		} catch (InvalidBeanException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isSaved;
	}

	private String gettingLargerQueryForDevRole(DeveloperRole devRole, String tableName, String devRoleQuery)
			throws InvalidBeanException, SQLException {
		if (devRoleQuery.isEmpty()) {
			devRoleQuery = ("insert into " + tableName + " (`merge_scenario_id`, `contributor_id`) values");
		}
		devRoleQuery = devRoleQuery + getNewDevRoleRow(devRole);

		return devRoleQuery;
	}

	private String getNewDevRoleRow(DeveloperRole devRole) {
		String result = " (" + devRole.getMergeScenarioIdDB() + ", " + devRole.getContributorIdDB() + "),";

		return result;

	}

}
