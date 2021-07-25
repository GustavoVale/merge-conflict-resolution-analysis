package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commnet.model.beans.FileMS;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class FileDao implements DAO<FileMS> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(FileMS file) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `files` ( `filepath`, `merge_scenarios_id`, `has_conflict`) values (?,?,?);");
			ps.setString(1, file.getFileName());
			ps.setInt(2, file.getMergeScenarioIdDB());
			ps.setBoolean(3, file.isConflict());
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
	public void delete(FileMS file) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `files` where `id`=?;");
			ps.setInt(1, file.getIdDB());
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
	public List<FileMS> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public FileMS get(FileMS file) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn
					.prepareStatement("select * from `files` where `id`=? or `filepath`=? and `merge_scenarios_id`=?;");

			if (file.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, file.getIdDB());
			}
			ps.setString(2, file.getFileName());
			ps.setInt(3, file.getMergeScenarioIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				String fileName = rs.getString("filepath");
				Integer msIdDB = rs.getInt("merge_scenarios_id");
				boolean hasConflict = rs.getBoolean("has_conflict");
				return new FileMS(idDB, fileName, msIdDB, hasConflict);
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
	public List<FileMS> search(FileMS object) throws InvalidBeanException {
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

	public List<Integer> getListOfFilesInMS(Integer msId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT id FROM files where merge_scenarios_id=" + msId + ";");
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

	public Integer getNumberOfConflictedFilesInMS(Integer msId) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT count(*) FROM files where merge_scenarios_id=" + msId + " AND has_conflict=1;");
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<Integer> getNumberFilebyMsAndSide(Integer msId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT files.id AS file_id FROM commits INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON chunks.file_id=files.id "
							+ "WHERE files.merge_scenarios_id=" + msId + " AND chunk_commits.side='" + side + "';");
			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("file_id"))) {
					result.add(rs.getInt("file_id"));
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

	public List<Integer> getFileListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT files.id as file_id from files INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id"
							+ " WHERE project_id=" + projectIdDB + ";");

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

	public Integer getDistinctFileNumberByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT count(distinct filepath) from files INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id"
							+ " WHERE project_id=" + projectIdDB + ";");

			rs = ps.executeQuery();

			if (rs.next()) {
				result = rs.getInt(1);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<String> getListOfFilesPathsInMS(Integer msId) throws InvalidBeanException, SQLException {
		List<String> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT filepath FROM files where merge_scenarios_id=" + msId + ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("filepath"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<String> getListOfFilesPathsChangedByCommitId(Integer commitId)
			throws InvalidBeanException, SQLException {
		List<String> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT distinct(filepath) FROM files inner join chunks on files.id=chunks.file_id"
							+ " inner join chunk_commits on chunk_commits.chunk_id=chunks.id inner join commits on "
							+ "commits.id=chunk_commits.commit_id where commits.id=" + commitId + ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("filepath"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public HashMap<String, String> getMapFilePathAndMergeCommitHash(Integer fileId) throws InvalidBeanException, SQLException {

		HashMap<String, String> result = new HashMap<>();

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT filepath, merge_commit_hash FROM files inner join merge_scenarios "
					+ "on merge_scenarios.id=files.merge_scenarios_id where files.id=" + fileId + ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getString("filepath"), rs.getString("merge_commit_hash"));
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
