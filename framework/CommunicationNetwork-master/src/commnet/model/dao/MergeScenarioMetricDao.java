package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.MergeScenarioMetrics;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class MergeScenarioMetricDao implements DAO<MergeScenarioMetrics> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(MergeScenarioMetrics msMetric) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `ms_metrics` ( `merge_scenario_id`, `number_of_files`, `number_conflicted_files`, `number_left_files`, "
							+ " `number_right_files`,  `number_both_side_files`, `number_chunks`, `number_conflicted_chunks`, "
							+ " `number_left_chunks`, `number_right_chunks`, `number_commits`, `number_left_commits`, "
							+ "`number_right_commits`, `number_developers`, `number_left_dev`, `number_right_dev`, `number_both_side_dev`)"
							+ " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

			ps.setInt(1, msMetric.getMsIdDB());
			ps.setInt(2, msMetric.getNumberFiles());
			ps.setInt(3, msMetric.getNumberConflictedFiles());
			ps.setInt(4, msMetric.getNumberLeftFiles());
			ps.setInt(5, msMetric.getNumberRightFiles());
			ps.setInt(6, msMetric.getNumberBothSideFiles());
			ps.setInt(7, msMetric.getNumberChunks());
			ps.setInt(8, msMetric.getNumberConflictedChunks());
			ps.setInt(9, msMetric.getNumberLeftChunks());
			ps.setInt(10, msMetric.getNumberRightChunks());
			ps.setInt(11, msMetric.getNumberCommits());
			ps.setInt(12, msMetric.getNumberLeftCommits());
			ps.setInt(13, msMetric.getNumberRightCommits());
			ps.setInt(14, msMetric.getNumberDevelopers());
			ps.setInt(15, msMetric.getNumberLeftDevelopers());
			ps.setInt(16, msMetric.getNumberRightDevelopers());
			ps.setInt(17, msMetric.getNumberBothSideDevelopers());
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
	public void delete(MergeScenarioMetrics object)throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from ms_metrics where id=" + object.getIdDB() + ";");
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
	public List<MergeScenarioMetrics> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MergeScenarioMetrics get(MergeScenarioMetrics msMetric) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `ms_metrics` where `id`=? or `merge_scenario_id`=?;");

			if (msMetric.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, msMetric.getIdDB());
			}
			ps.setInt(2, msMetric.getMsIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer msIdDB = rs.getInt("merge_scenario_id");

				return new MergeScenarioMetrics(idDB, msIdDB);
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
	public List<MergeScenarioMetrics> search(MergeScenarioMetrics object) throws InvalidBeanException {
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

	public List<Integer> getMsMetricsListMergeScenarioIdByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT ms_metrics.merge_scenario_id FROM ms_metrics INNER JOIN merge_scenarios ON "
							+ "merge_scenarios.id=ms_metrics.merge_scenario_id WHERE merge_scenarios.project_id="
							+ projectIdDB + ";");

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
	
	public List<Integer> getMsMetricsListIdByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT ms_metrics.id FROM ms_metrics INNER JOIN merge_scenarios ON "
							+ "merge_scenarios.id=ms_metrics.merge_scenario_id WHERE merge_scenarios.project_id="
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

	public boolean saveManyRows(String msMetricRowList)throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(msMetricRowList);
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
