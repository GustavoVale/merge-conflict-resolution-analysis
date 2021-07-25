package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.NetworkMetrics;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class NetworkMetricDao implements DAO<NetworkMetrics> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(NetworkMetrics netCoverageMetrics) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `net_metrics` ( `network_id`, `coverage_chunk_comprehensive`, `coverage_chunk_precise`, `coverage_file_comprehensive`, "
							+ " `coverage_file_precise`,  `coverage_ms_comprehensive`, `coverage_ms_precise`, `coverage_comprehensive_chunk`, "
							+ " `coverage_precise_chunk`, `coverage_comprehensive_file`, `coverage_precise_file`, `coverage_comprehensive_ms`, "
							+ "`coverage_precise_ms`, `number_comprehensive_edges`, `number_precise_edges` ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

			ps.setInt(1, netCoverageMetrics.getNetworkIdDB());
			ps.setInt(2, netCoverageMetrics.getCovChunkBasedOverComprehensive());
			ps.setInt(3, netCoverageMetrics.getCovChunkBasedOverPrecise());
			ps.setInt(4, netCoverageMetrics.getCovFileBasedOverComprehensive());
			ps.setInt(5, netCoverageMetrics.getCovFileBasedOverPrecise());
			ps.setInt(6, netCoverageMetrics.getCovMSBasedOverComprehensive());
			ps.setInt(7, netCoverageMetrics.getCovMSBasedOverPrecise());
			ps.setInt(8, netCoverageMetrics.getCovComprehensiveOverChunkBased());
			ps.setInt(9, netCoverageMetrics.getCovPreciseOverChunkBased());
			ps.setInt(10, netCoverageMetrics.getCovComprehensiveOverFileBased());
			ps.setInt(11, netCoverageMetrics.getCovPreciseOverFileBased());
			ps.setInt(12, netCoverageMetrics.getCovComprehensiveOverMSBased());
			ps.setInt(13, netCoverageMetrics.getCovPreciseOverFileBased());
			ps.setInt(14, netCoverageMetrics.getNumberComprehensiveEdges());
			ps.setInt(15, netCoverageMetrics.getNumberPreciseEdges());
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

	@Override
	public void delete(NetworkMetrics object) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from net_metrics where id=" + object.getIdDB() + ";");
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
	public List<NetworkMetrics> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkMetrics get(NetworkMetrics netCoverageMetrics) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `net_metrics` where `id`=? or `network_id`=?;");

			if (netCoverageMetrics.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, netCoverageMetrics.getIdDB());
			}
			ps.setInt(2, netCoverageMetrics.getNetworkIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer networkIdDB = rs.getInt("network_id");

				return new NetworkMetrics(idDB, networkIdDB);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	@Override
	public List<NetworkMetrics> search(NetworkMetrics object) throws InvalidBeanException {
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

	public List<Integer> getNetMetricsNetworkIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT net_metrics.network_id FROM net_metrics INNER JOIN networks ON net_metrics.network_id=networks.id INNER JOIN "
							+ "merge_scenarios ON merge_scenarios.id=networks.merge_scenarios_id WHERE "
							+ "merge_scenarios.project_id=" + projectIdDB + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("network_id"));
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;

	}
	
	public List<Integer> getNetMetricsIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT net_metrics.id FROM net_metrics INNER JOIN networks ON net_metrics.network_id=networks.id INNER JOIN "
							+ "merge_scenarios ON merge_scenarios.id=networks.merge_scenarios_id WHERE "
							+ "merge_scenarios.project_id=" + projectIdDB + ";");

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

	public boolean saveManyRows(String netMetricRowList) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(netMetricRowList);
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

}
