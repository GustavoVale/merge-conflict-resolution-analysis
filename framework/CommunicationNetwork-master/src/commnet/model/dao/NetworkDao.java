package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.Network;
import commnet.model.dao.validators.NetworkValidator;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class NetworkDao implements DAO<Network> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(Network contNet) throws InvalidBeanException, SQLException {
		NetworkValidator validator = new NetworkValidator();
		boolean hasSaved = false;
		validator.validate(contNet);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `networks` ( `merge_scenarios_id`) values (?);");

			ps.setInt(1, contNet.getmsIdDB());
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

	@Override
	public void delete(Network object) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from networks where id=" + object.getIdDB() + ";");
			ps.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
	}

	@Override
	public List<Network> list() throws InvalidBeanException, SQLException {
		List<Network> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `networks`;");
			rs = ps.executeQuery();

			while (rs.next()) {
				Network contNet = new Network();
				contNet.setIdDB(rs.getInt("id"));
				result.add(contNet);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public Network get(Network contNet) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `networks` where `id`=? or `merge_scenarios_id`=?;");
			if (contNet.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, contNet.getIdDB());
			}
			ps.setInt(2, contNet.getmsIdDB());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer msID = rs.getInt("merge_scenarios_id");
				return new Network(id, msID);

			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public List<Integer> getMSIdForNetworkComputedByProject(Integer projectId, List<Integer> netTypes)
			throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		String query = gettingQuery(projectId, netTypes);

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(query);

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("id"));
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	private String gettingQuery(Integer projectId, List<Integer> netTypes) {
		String query = "SELECT distinct(merge_scenarios.id) from networks INNER JOIN merge_scenarios ON "
				+ "networks.merge_scenarios_id=merge_scenarios.id INNER JOIN edges ON "
				+ "edges.network_id=networks.id WHERE merge_scenarios.project_id=" + projectId + ";";
		if (!netTypes.isEmpty()) {
			query = query.substring(0, query.length() - 1);
			query = query + " AND (";
			for (Integer i : netTypes) {
				query = query + "edges.type=" + i + " OR ";
			}
			query = query.substring(0, query.length() - 4);
			query = query + ");";
		}
		return query;
	}

	@Override
	public List<Network> search(Network object) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}

	public List<Integer> getNetworkIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT networks.id from networks INNER JOIN merge_scenarios ON "
					+ "networks.merge_scenarios_id=merge_scenarios.id WHERE project_id=" + projectIdDB + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("id"));
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<Network> getNetworkListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Network> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT networks.* from networks INNER JOIN merge_scenarios ON "
					+ "networks.merge_scenarios_id=merge_scenarios.id WHERE project_id=" + projectIdDB + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(new Network(rs.getInt("id"), rs.getInt("merge_scenarios_id")));
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

}
