package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.FileMetrics;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class FileMetricDao implements DAO<FileMetrics> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(FileMetrics fileMetric) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `file_metrics` ( `file_id`, `number_chunks`, `number_conflicted_chunks`, `number_left_chunks`, "
							+ "`number_right_chunks`, `number_commits`, `number_left_commits`, `number_right_commits`,"
							+ " `number_developers`, `number_left_dev`, `number_right_dev`, `number_both_side_dev`, `loc`)"
							+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?);");

			ps.setInt(1, fileMetric.getFileIdDB());
			ps.setInt(2, fileMetric.getNumberChunks());
			ps.setInt(3, fileMetric.getNumberConflictedChunks());
			ps.setInt(4, fileMetric.getNumberLeftChunks());
			ps.setInt(5, fileMetric.getNumberRightChunks());
			ps.setInt(6, fileMetric.getNumberCommits());
			ps.setInt(7, fileMetric.getNumberLeftCommits());
			ps.setInt(8, fileMetric.getNumberRightCommits());
			ps.setInt(9, fileMetric.getNumberDevelopers());
			ps.setInt(10, fileMetric.getNumberLeftDevelopers());
			ps.setInt(11, fileMetric.getNumberRightDevelopers());
			ps.setInt(12, fileMetric.getNumberBothSideDevelopers());
			ps.setInt(13, fileMetric.getLoc());
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
	public void delete(FileMetrics object) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from file_metrics where id=" + object.getIdDB() + ";");
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
	public List<FileMetrics> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileMetrics get(FileMetrics fileMetric) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `file_metrics` where `id`=? or `file_id`=?;");

			if (fileMetric.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, fileMetric.getIdDB());
			}
			ps.setInt(2, fileMetric.getFileIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer fileIdDB = rs.getInt("file_id");

				return new FileMetrics(idDB, fileIdDB);
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
	public List<FileMetrics> search(FileMetrics object) throws InvalidBeanException {
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

	public List<Integer> getFileMetricsFileIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT file_metrics.file_id as file_id FROM file_metrics INNER JOIN files ON files.id=file_metrics.file_id"
							+ " INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id WHERE merge_scenarios.project_id="
							+ projectIdDB + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("file_id"));
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}
	
	public List<Integer> getFileMetricsIdListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT file_metrics.id FROM file_metrics INNER JOIN files ON files.id=file_metrics.file_id"
							+ " INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id WHERE merge_scenarios.project_id="
							+ projectIdDB + ";");

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

	public boolean saveManyRows(String fileMetricRowList) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(fileMetricRowList);
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
