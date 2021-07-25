package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.ChunkMetrics;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ChunkMetricDao implements DAO<ChunkMetrics> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(ChunkMetrics chunkMetric) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `chunk_metrics` ( `chunk_id`, `number_commits`, `number_left_commits`, "
							+ "`number_right_commits`, `number_developers`, `number_left_dev`, `number_right_dev`, "
							+ "`number_both_side_dev`) values (?,?,?,?,?,?,?,?);");

			ps.setInt(1, chunkMetric.getChunkIdDB());
			ps.setInt(2, chunkMetric.getNumberCommits());
			ps.setInt(3, chunkMetric.getNumberLeftCommits());
			ps.setInt(4, chunkMetric.getNumberRightCommits());
			ps.setInt(5, chunkMetric.getNumberDevelopers());
			ps.setInt(6, chunkMetric.getNumberLeftDevelopers());
			ps.setInt(7, chunkMetric.getNumberRightDevelopers());
			ps.setInt(8, chunkMetric.getNumberBothSideDevelopers());
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
	public void delete(ChunkMetrics object) throws InvalidBeanException, SQLException {
			try {
				conn = Database.getConnection();
				ps = conn.prepareStatement("delete from chunk_metrics where id=" + object.getIdDB() + ";");
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
	public List<ChunkMetrics> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChunkMetrics get(ChunkMetrics chunkMetric) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `chunk_metrics` where `id`=? or `chunk_id`=?;");

			if (chunkMetric.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, chunkMetric.getIdDB());
			}
			ps.setInt(2, chunkMetric.getChunkIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer chunkIdDB = rs.getInt("chunk_id");

				return new ChunkMetrics(idDB, chunkIdDB);
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
	public List<ChunkMetrics> search(ChunkMetrics object) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResources() throws SQLException {
		// TODO Auto-generated method stub

	}

	public List<Integer> getChunkMetricsChunkIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunk_metrics.chunk_id as chunk_id FROM chunk_metrics INNER JOIN chunks ON "
							+ "chunks.id=chunk_metrics.chunk_id INNER JOIN files ON files.id=chunks.file_id "
							+ "INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id "
							+ "WHERE merge_scenarios.project_id=" + projectIdDB + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("chunk_id"));
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}
	
	public List<Integer> getChunkMetricsIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunk_metrics.id FROM chunk_metrics INNER JOIN chunks ON "
							+ "chunks.id=chunk_metrics.chunk_id INNER JOIN files ON files.id=chunks.file_id "
							+ "INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id "
							+ "WHERE merge_scenarios.project_id=" + projectIdDB + ";");

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

	public boolean saveManyRows(String chunkMetricRowList) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(chunkMetricRowList);
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
