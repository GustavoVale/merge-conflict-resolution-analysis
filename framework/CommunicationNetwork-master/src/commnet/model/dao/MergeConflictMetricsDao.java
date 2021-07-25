package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.Calendar;
import java.util.List;

import commnet.model.beans.MergeConflictMetrics;
import commnet.model.db.Database;
import commnet.model.enums.ChangeType;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class MergeConflictMetricsDao implements DAO<MergeConflictMetrics> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(MergeConflictMetrics mergeConflictMetrics) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `merge_conflict_metrics` ( `merge_conflict_info_id`, `change_type`, `loc`, "
							+ "`left_loc`, `right_loc`, `cyclomatic_complexity`, `left_cyclomatic_complexity`, "
							+ "`right_cyclomatic_complexity`, `dev_has_knowledge`) values (?,?,?,?,?,?,?,?,?);");
			ps.setInt(1, mergeConflictMetrics.getMergeConflictInfoIdDB());
			ps.setString(2, "OT");
			ps.setInt(3, 0);
			ps.setInt(4, 0);
			ps.setInt(5, 0);
			ps.setInt(6, 0);
			ps.setInt(7, 0);
			ps.setInt(8, 0);
			ps.setBoolean(9, mergeConflictMetrics.isDevHasKnowledge());
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
	public void delete(MergeConflictMetrics mergeConflictMetrics) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `merge_conflict_code` where `id`=?;");
			ps.setInt(1, mergeConflictMetrics.getIdDB());
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
	public List<MergeConflictMetrics> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean mergeConflictMetricsIdAlreadyExist(Integer id) throws InvalidBeanException, SQLException {
		boolean mergeConflictMetricsIDExist = false;
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `merge_conflict_metrics` where `merge_conflict_info_id`=?;");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				mergeConflictMetricsIDExist = true;
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return mergeConflictMetricsIDExist;
	}

	@Override
	public MergeConflictMetrics get(MergeConflictMetrics mergeConflictMetrics)
			throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"select * from `merge_conflict_metrics` where `id`=? or `merge_conflict_info_id`=?;");

			if (mergeConflictMetrics.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, mergeConflictMetrics.getIdDB());
			}
			ps.setInt(2, mergeConflictMetrics.getMergeConflictInfoIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer mergeConflictInfo = rs.getInt("merge_conflict_info_id");
				ChangeType changeType = ChangeType.valueOf(rs.getString("change_type"));
				Integer loc = rs.getInt("loc");
				Integer leftLoc = rs.getInt("left_loc");
				Integer rightLoc = rs.getInt("right_loc");
				Integer complexity = rs.getInt("cyclomatic_complexity");
				Integer leftComplexity = rs.getInt("left_cyclomatic_complexity");
				Integer rightComplexity = rs.getInt("right_cyclomatic_complexity");
				boolean devKnowledge = rs.getBoolean("dev_has_knowledge");

				return (new MergeConflictMetrics(id, mergeConflictInfo, changeType, loc, leftLoc, rightLoc, complexity,
						leftComplexity, rightComplexity, devKnowledge));
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
	public List<MergeConflictMetrics> search(MergeConflictMetrics object) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<Integer, ArrayList<MergeConflictMetrics>> getListMergeConflictMetrics(Integer projectIdDB)
			throws InvalidBeanException, SQLException {

		HashMap<Integer, ArrayList<MergeConflictMetrics>> mapMergeScenarios = new HashMap<>();

		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"SELECT merge_scenarios.project_id as pr_id, merge_scenarios.id as ms_id, merge_scenarios.commit_merge,"
							+ " filepath, merge_conflict_metrics.* from merge_scenarios inner join files on "
							+ "files.merge_scenarios_id=merge_scenarios.id inner join chunks on chunks.file_id=files.id inner join "
							+ "merge_conflict_info on chunks.id=merge_conflict_info.chunk_id inner join merge_conflict_metrics on "
							+ "merge_conflict_info.id=merge_conflict_metrics.merge_conflict_info_id where project_id="
							+ projectIdDB + " and files.has_conflict>0;");
			rs = ps.executeQuery();
			while (rs.next()) {

				MergeConflictMetrics mergeConflictMetrics = new MergeConflictMetrics(rs.getInt("id"),
						rs.getInt("merge_conflict_info_id"), ChangeType.fromString(rs.getString("change_type")),
						rs.getInt("loc"), rs.getInt("left_loc"), rs.getInt("right_loc"),
						rs.getInt("cyclomatic_complexity"), rs.getInt("left_cyclomatic_complexity"),
						rs.getInt("right_cyclomatic_complexity"), rs.getBoolean("dev_has_knowledge"),
						rs.getInt("pr_id"), rs.getInt("ms_id"), rs.getInt("commit_merge"), rs.getString("filepath"),
						null);

				if (mapMergeScenarios.isEmpty()) {

					mapMergeScenarios.put(rs.getInt("ms_id"),
							new ArrayList<MergeConflictMetrics>(Arrays.asList(mergeConflictMetrics)));
				} else {
					if (mapMergeScenarios.containsKey(rs.getInt("ms_id"))) {
						mapMergeScenarios.get(rs.getInt("ms_id")).add(mergeConflictMetrics);
					} else {
						mapMergeScenarios.put(rs.getInt("ms_id"),
								new ArrayList<MergeConflictMetrics>(Arrays.asList(mergeConflictMetrics)));
					}
				}

			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();

		} finally {
			closeResources();
		}
		return mapMergeScenarios;
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

	@Override
	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}

}
