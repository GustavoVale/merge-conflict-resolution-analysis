package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commnet.model.beans.MergeConflictInfo;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class MergeConflictInfoDao implements DAO<MergeConflictInfo> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(MergeConflictInfo mergeConflictInfo) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `merge_conflict_info` ( `chunk_id`, `left_commit_id`, `right_commit_id`, "
							+ "`left_code`, `right_code`, `merged_code`, `left_merge_time_diff`, `right_merge_time_diff`) values (?,?,?,?,?,?,?,?);");
			ps.setInt(1, mergeConflictInfo.getChunkIdDB());
			ps.setInt(2, mergeConflictInfo.getLeftCommitIdDB());
			ps.setInt(3, mergeConflictInfo.getRightCommitIdDB());
			ps.setString(4, mergeConflictInfo.getLeftCommitCode());
			ps.setString(5, mergeConflictInfo.getRightCommitCode());
			ps.setString(6, mergeConflictInfo.getMergedCode());
			ps.setLong(7, mergeConflictInfo.getLeftMergeTimeDifference());
			ps.setLong(8, mergeConflictInfo.getRightMergeTimeDifference());
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
	public void delete(MergeConflictInfo mergeConflictInfo) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `merge_conflict_code` where `id`=?;");
			ps.setInt(1, mergeConflictInfo.getIdDB());
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
	public List<MergeConflictInfo> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MergeConflictInfo get(MergeConflictInfo mergeConflictInfo) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `merge_conflict_info` where `id`=? or `chunk_id`=?;");

			if (mergeConflictInfo.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, mergeConflictInfo.getIdDB());
			}
			if (mergeConflictInfo.getChunkIdDB() == null) {
				ps.setInt(2, 0);
			} else {
				ps.setInt(2, mergeConflictInfo.getChunkIdDB());
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer chunkID = rs.getInt("chunk_id");
				Integer leftCommitId = rs.getInt("left_commit_id");
				Integer rightCommitId = rs.getInt("right_commit_id");
				String leftCode = rs.getString("left_code");
				String rightCode = rs.getString("right_code");
				String mergedCode = rs.getString("merged_code");
				long leftTimeDiff = rs.getLong("left_merge_time_diff");
				long rightTimeDiff = rs.getLong("right_merge_time_diff");

				return (new MergeConflictInfo(id, chunkID, leftCommitId, rightCommitId, leftCode, rightCode, mergedCode,
						leftTimeDiff, rightTimeDiff));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public HashMap<Integer, String> getListMergeConflictInfoIdsByMergeScenario(Integer mergeScenarioIdDB)
			throws InvalidBeanException, SQLException {

		HashMap<Integer, String> result = new HashMap<>();
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"SELECT merge_conflict_info.id as mc_info_id, files.filepath from merge_scenarios inner join files on "
							+ "files.merge_scenarios_id=merge_scenarios.id inner join chunks on chunks.file_id=files.id inner join "
							+ "merge_conflict_info on chunks.id=merge_conflict_info.chunk_id where merge_scenarios.id="
							+ mergeScenarioIdDB + ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getInt("mc_info_id"), rs.getString("filepath"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();

		} finally {
			closeResources();
		}
		return result;
	}

	@Override
	public List<MergeConflictInfo> search(MergeConflictInfo mergeConflictInfo) throws InvalidBeanException {
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

	public Map<Integer, MergeConflictInfo> getMciByMsId(Integer msId) throws InvalidBeanException, SQLException {

		Map<Integer, MergeConflictInfo> result = new HashMap<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select merge_conflict_info.* from merge_scenarios inner join files on merge_scenarios.id=files.merge_scenarios_id "
							+ "inner join chunks on chunks.file_id=files.id inner join merge_conflict_info on chunks.id = "
							+ "merge_conflict_info.chunk_id where merge_scenarios.id=" + msId + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.put(rs.getInt("id"),
						new MergeConflictInfo(rs.getInt("id"), rs.getInt("chunk_id"), rs.getInt("left_commit_id"),
								rs.getInt("right_commit_id"), rs.getString("left_code"), rs.getString("right_code"),
								rs.getString("merged_code"), rs.getLong("left_merge_time_diff"),
								rs.getLong("right_merge_time_diff")));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();

		} finally {
			closeResources();
		}
		return result;
	}

	public boolean insertCode(int chunkID, String code, String columnName, boolean concatenate)
			throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		String value = concatenate ? "CONCAT(" + columnName + ",?)" : "?";
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("UPDATE `merge_conflict_info` SET " + columnName + "=" + value
					+ " where merge_conflict_info.chunk_id = " + chunkID + ";");
			ps.setString(1, code);
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

	public boolean saveWithoutCode(MergeConflictInfo mergeConflictInfo) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `merge_conflict_info` ( `chunk_id`, `left_commit_id`, `right_commit_id`, "
							+ "`left_code`, `right_code`, `merged_code`, `left_merge_time_diff`, `right_merge_time_diff`) values (?,?,?,?,?,?,?,?);");
			ps.setInt(1, mergeConflictInfo.getChunkIdDB());
			ps.setInt(2, mergeConflictInfo.getLeftCommitIdDB());
			ps.setInt(3, mergeConflictInfo.getRightCommitIdDB());
			ps.setString(4, "");
			ps.setString(5, "");
			ps.setString(6, "");
			ps.setLong(7, mergeConflictInfo.getLeftMergeTimeDifference());
			ps.setLong(8, mergeConflictInfo.getRightMergeTimeDifference());
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
