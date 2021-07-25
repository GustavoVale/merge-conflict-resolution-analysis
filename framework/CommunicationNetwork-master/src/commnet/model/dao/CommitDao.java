package commnet.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commnet.model.beans.CommitN;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class CommitDao implements DAO<CommitN> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(CommitN commit) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `commits` ( `project_id`, `contributor_id`, `committer_id`, `hash`, `commit_date`) values (?,?,?,?,?);");
			ps.setInt(1, commit.getProjectIdDB());
			ps.setInt(2, commit.getContIdDB());
			ps.setInt(3, commit.getCommitterIdDB());
			ps.setString(4, commit.getHash());
			@SuppressWarnings("deprecation")
			String formattedDate = getFormattedDate(commit.getCommitDate().toGMTString());
			ps.setString(5, formattedDate);
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
	public void delete(CommitN commit) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `commits` where `id`=?;");
			ps.setInt(1, commit.getIdDB());
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
	public List<CommitN> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public CommitN get(CommitN commit) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `commits` where `id`=? or `project_id`=? and `hash`=?;");

			if (commit.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, commit.getIdDB());
			}
			ps.setInt(2, commit.getProjectIdDB());
			ps.setString(3, commit.getHash());

			rs = ps.executeQuery();
			if (rs.next()) {
				Timestamp timeStamp = rs.getTimestamp("commit_date");
				commit.setIdDB(rs.getInt("id"));

				if (timeStamp.getMinutes() == 0 && timeStamp.getSeconds() == 0) {
					closeResources();
					this.updateCommit(commit);
					return commit;
				}

				Integer idDB = rs.getInt("id");
				Integer projectID = rs.getInt("project_id");
				Integer contributorID = rs.getInt("contributor_id");
				Integer commiterID = rs.getInt("committer_id");
				String hash = rs.getString("hash");
				Date date = rs.getDate("commit_date");
				return new CommitN(idDB, projectID, contributorID, commiterID, hash, date);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	/**
	 * Get a Commit in the database based on his hash
	 * 
	 * @param commitHash
	 * @return Commit id in the database if it finds, false otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public Integer getByHash(String commitHash) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `commits` where `hash`=?;");
			ps.setString(1, commitHash);

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				return idDB;
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
	public List<CommitN> search(CommitN object) throws InvalidBeanException {
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

	public Map<Integer, String> getCommitsByContId(int contIdDB) throws InvalidBeanException, SQLException {
		Map<Integer, String> result = new HashMap<Integer, String>();
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select commits.id, commits.hash from `commits` where `contributor_id`=?;");
			ps.setInt(1, contIdDB);

			rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getInt("id"), rs.getString("hash"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public Integer getCommitsByMs(Integer msId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commits.id as commit_id from `commits` INNER JOIN chunk_commits ON"
					+ " commits.id=chunk_commits.commit_id INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON "
					+ "chunks.file_id=files.id WHERE files.merge_scenarios_id=" + msId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("commit_id"))) {
					result.add(rs.getInt("commit_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result.size();
	}

	public Integer getCommitsByMsAndSide(Integer msId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commits.id as commit_id from `commits` INNER JOIN chunk_commits ON"
					+ " commits.id=chunk_commits.commit_id INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON "
					+ "chunks.file_id=files.id WHERE files.merge_scenarios_id=" + msId + " AND chunk_commits.side='"
					+ side + "';");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("commit_id"))) {
					result.add(rs.getInt("commit_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result.size();
	}

	public Integer getCommitsByFile(Integer fileId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commits.id as commit_id from `commits` INNER JOIN chunk_commits ON"
					+ " commits.id=chunk_commits.commit_id INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id "
					+ "WHERE chunks.file_id=" + fileId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("commit_id"))) {
					result.add(rs.getInt("commit_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result.size();
	}

	public Integer getCommitsByFileAndSide(Integer fileId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commits.id as commit_id from `commits` INNER JOIN chunk_commits ON"
					+ " commits.id=chunk_commits.commit_id INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id "
					+ "WHERE chunks.file_id=" + fileId + " AND chunk_commits.side='" + side + "';");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("commit_id"))) {
					result.add(rs.getInt("commit_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result.size();
	}

	public Integer getCommitsByChunk(Integer chunkId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commits.id as commit_id from `commits` INNER JOIN chunk_commits ON"
					+ " commits.id=chunk_commits.commit_id WHERE chunk_commits.chunk_id=" + chunkId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("commit_id"))) {
					result.add(rs.getInt("commit_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result.size();
	}

	public Integer getCommitsByChunkAndSide(Integer chunkId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commits.id as commit_id from `commits` INNER JOIN chunk_commits ON"
					+ " commits.id=chunk_commits.commit_id WHERE chunk_commits.chunk_id=" + chunkId
					+ " AND chunk_commits.side='" + side + "';");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("commit_id"))) {
					result.add(rs.getInt("commit_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result.size();
	}

	public Integer getNumberOfCommitsByProject(Integer projectId) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select count(*) from `commits` WHERE project_id=" + projectId + ";");

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

	public List<CommitN> getCommitsByProject(Integer projectId) throws InvalidBeanException, SQLException {
		List<CommitN> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `commits` WHERE project_id=" + projectId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer projectID = rs.getInt("project_id");
				Integer contributorID = rs.getInt("contributor_id");
				Integer committerID = rs.getInt("committer_id");
				String hash = rs.getString("hash");
				Date date = rs.getDate("commit_date");
				Timestamp fullDate = rs.getTimestamp("commit_date");
				CommitN commit = new CommitN(idDB, projectID, contributorID, committerID, hash, date, fullDate);

				result.add(commit);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public boolean updateManyRows(String commitRowList) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(commitRowList);
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

	@SuppressWarnings("deprecation")
	private boolean updateCommit(CommitN commit) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("UPDATE `commits` SET `commit_date`= \""
					+ this.getFormattedDate(commit.getCommitDate().toGMTString()) + "\" where id=" + commit.getIdDB()
					+ ";");
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

	public boolean updateCommitterIdDB(CommitN commit) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("UPDATE `commits` SET `committer_id`=" + commit.getCommitterIdDB() + " where id="
					+ commit.getIdDB() + ";");
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

	public CommitN getByIdDB(Integer commitIdDB) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `commits` where `id`=?;");
			ps.setInt(1, commitIdDB);

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer projectID = rs.getInt("project_id");
				Integer contributorID = rs.getInt("contributor_id");
				Integer commiterID = rs.getInt("committer_id");
				String hash = rs.getString("hash");
				Date date = rs.getDate("commit_date");
				return new CommitN(idDB, projectID, contributorID, commiterID, hash, date);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public HashMap<Integer, Integer> getMapOfMergeScenarioIDsAndMergeCommitsIDsOfConflictingMergeScenariosPerProject(
			Integer projectId) throws InvalidBeanException, SQLException {
		HashMap<Integer, Integer> result = new HashMap<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT id, commit_merge FROM merge_scenarios where has_conflict = 1 AND project_id=" + projectId
							+ ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				result.put(rs.getInt("id"), rs.getInt("commit_merge"));
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
