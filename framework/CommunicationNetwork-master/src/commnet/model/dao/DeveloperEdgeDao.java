package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.DeveloperNode;
import commnet.model.dao.DAOFactory.Bean;
import commnet.model.dao.validators.DeveloperEdgeValidator;
import commnet.model.db.Database;
import commnet.model.enums.EdgeSide;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class DeveloperEdgeDao implements DAO<DeveloperEdge> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(DeveloperEdge edge) throws InvalidBeanException, SQLException {
		DeveloperEdgeValidator validator = new DeveloperEdgeValidator();
		boolean hasSaved = false;
		validator.validate(edge);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `edges` (`network_id`, `dev_a`, `dev_b`, `type`, `side`, `weight`) values (?,?,?,?,?,?);");
			ps.setInt(1, edge.getNetworkID());
			ps.setInt(2, edge.getDevA().getIdDB());
			ps.setInt(3, edge.getDevB().getIdDB());
			ps.setInt(4, edge.getEdgeType());
			ps.setString(5, edge.getEdgeSide().toString());
			ps.setInt(6, edge.getWeight());
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
	public void delete(DeveloperEdge object) throws InvalidBeanException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<DeveloperEdge> list() throws InvalidBeanException, SQLException {
		List<DeveloperEdge> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `edges`;");
			rs = ps.executeQuery();

			while (rs.next()) {
				DeveloperEdge edge = new DeveloperEdge();
				edge.setIdDB(rs.getInt("id"));
				edge.setNetworkIdDB(rs.getInt("network_id"));
				DeveloperNode aux = new DeveloperNodeDao().get(new DeveloperNode(rs.getInt("dev_a"), null, null));
				edge.setDevA(aux);
				aux = new DeveloperNodeDao().get(new DeveloperNode(rs.getInt("dev_b"), null, null));
				edge.setDevB(aux);
				edge.setWeight(rs.getInt("weight"));

				result.add(edge);
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
	public DeveloperEdge get(DeveloperEdge edge) throws InvalidBeanException, SQLException {
		try {
			DeveloperNodeDao dndao = (DeveloperNodeDao) DAOFactory.getDAO(Bean.NODE);
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select * from `edges` where (`id`=? or `network_id`=? and `dev_a`=? and `dev_b`=? and `type`=? and `side`=?) ;");

			if (edge.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, edge.getIdDB());
			}
			ps.setInt(2, edge.getNetworkID());
			if (edge.getDevA().getIdDB() == null) {
				edge.getDevA().setIdDB(dndao.getDevIdDB(edge.getDevA()));
				ps.setInt(3, edge.getDevA().getIdDB());
			} else {
				ps.setInt(3, edge.getDevA().getIdDB());
			}
			if (edge.getDevB().getIdDB() == null) {
				edge.getDevB().setIdDB(dndao.getDevIdDB(edge.getDevB()));
				ps.setInt(4, edge.getDevB().getIdDB());
			} else {
				ps.setInt(4, edge.getDevB().getIdDB());
			}
			ps.setInt(5, edge.getEdgeType());
			ps.setString(6, edge.getEdgeSide().toString());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer networkid = rs.getInt("network_id");
				DeveloperNode deva = new DeveloperNodeDao().get(new DeveloperNode(rs.getInt("dev_a"), null, null));
				DeveloperNode devb = new DeveloperNodeDao().get(new DeveloperNode(rs.getInt("dev_b"), null, null));
				Integer type = rs.getInt("type");
				String sideString = rs.getString("side");
				EdgeSide side = EdgeSide.fromString(sideString);
				Integer weight = rs.getInt("weight");
				return new DeveloperEdge(id, networkid, deva, devb, type, side, weight);

			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public List<DeveloperEdge> getEdgeListByNetworkIdAndEdgeType(Integer netId, Integer edgeType)
			throws InvalidBeanException, SQLException {
		List<DeveloperEdge> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT * FROM edges WHERE network_id=" + netId + " AND type=" + edgeType + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				DeveloperEdge edge = new DeveloperEdge();
				edge.setIdDB(rs.getInt("id"));
				edge.setNetworkIdDB(rs.getInt("network_id"));
				edge.setDevA(new DeveloperNode(rs.getInt("dev_a"), null));
				edge.setDevB(new DeveloperNode(rs.getInt("dev_b"), null));
				edge.setEdgeType(rs.getInt("type"));
				edge.setWeight(rs.getInt("weight"));
				edge.setEdgeSide(EdgeSide.fromString(rs.getString("side")));

				result.add(edge);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<Integer> getEdgeIDListByProjectId(Integer projectId, List<Integer> edgeTypeList)
			throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(gettingQuery(projectId, edgeTypeList));

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

	private String gettingQuery(Integer projectId, List<Integer> edgeTypeList) {
		String query = "SELECT edges.id FROM edges inner join networks on networks.id=edges.network_id "
				+ "inner join merge_scenarios on merge_scenarios.id=networks.merge_scenarios_id "
				+ "where merge_scenarios.project_id= " + projectId + ";";

		if (!edgeTypeList.isEmpty()) {
			query = query.substring(0, query.length() - 1);
			query = query + " AND (";
			for (Integer i : edgeTypeList) {
				query = query + "edges.type=" + i + " OR ";
			}
			query = query.substring(0, query.length() - 4);
			query = query + ");";
		}
		return query;
	}

	@Override
	public List<DeveloperEdge> search(DeveloperEdge object) throws InvalidBeanException {
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

	public boolean saveManyRows(String edgeRowList) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(edgeRowList);
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

	public boolean deleteEdgeById(String query) throws SQLException {
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

	public List<Integer> getEdgeListByNetworkId(Integer idDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select id from `edges` where `network_id`=" + idDB + ";");
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

	public List<Integer> getDistinctDevByNetworkId(String dev, Integer networkId, List<Integer> edgeTypeList)
			throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(gettingFullQuery(dev, networkId, edgeTypeList));

			rs = ps.executeQuery();

			while (rs.next()) {
				if (dev.equals("dev_a")){
					result.add(rs.getInt("dev_a"));
				}else{
					result.add(rs.getInt("dev_b"));
				}
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	private String gettingFullQuery(String dev, Integer networkId, List<Integer> edgeTypeList) {
		String query = "SELECT distinct(" + dev + ") FROM edges where network_id= " + networkId + " AND (";

		for (Integer i : edgeTypeList) {
			query = query + "edges.type=" + i + " OR ";
		}
		query = query.substring(0, query.length() - 4);
		query = query + ");";
		return query;
	}

}
