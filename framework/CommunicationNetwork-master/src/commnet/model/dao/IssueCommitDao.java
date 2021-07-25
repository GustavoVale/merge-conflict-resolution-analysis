package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class IssueCommitDao {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private Integer idDB;
	private Integer commitIdDB;
	private Integer issueIdDB;

	public IssueCommitDao() {

	}

	public IssueCommitDao(Integer id, Integer commitId, Integer issueId) {
		setIdDB(id);
		setCommitIdDB(commitId);
		setIssueIdDB(issueId);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getCommitIdDB() {
		return commitIdDB;
	}

	public void setCommitIdDB(Integer commitIdDB) {
		this.commitIdDB = commitIdDB;
	}

	public Integer getIssueIdDB() {
		return issueIdDB;
	}

	public void setIssueIdDB(Integer issueIdDB) {
		this.issueIdDB = issueIdDB;
	}

	/**
	 * Get the Commit related to an Issue based on its database id or commit_id
	 * and issue_id
	 * 
	 * @param issueCommitDao
	 * @return Object if find, null otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public IssueCommitDao get(IssueCommitDao issueCommitDao) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from issue_commits where id=? or commit_id=? and issue_id=?;");
			if (issueCommitDao.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, issueCommitDao.getIdDB());
			}
			ps.setInt(2, issueCommitDao.getCommitIdDB());
			ps.setInt(3, issueCommitDao.getIssueIdDB());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer commitIdDB = rs.getInt("commit_id");
				Integer issueIdDB = rs.getInt("issue_id");
				return new IssueCommitDao(idDB, commitIdDB, issueIdDB);
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
	 * Save the commits related to an Issue
	 * 
	 * @param relatedCommit
	 * @return true if it saves, false otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public boolean save(IssueCommitDao relatedCommit) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `issue_commits` (`commit_id`, `issue_id`) values (?,?);");
			ps.setInt(1, relatedCommit.getCommitIdDB());
			ps.setInt(2, relatedCommit.getIssueIdDB());
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

	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}

	public boolean isInIssueCommitTable(Integer commitId) throws InvalidBeanException, SQLException {
		boolean result = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from issue_commits where commit_id=" + commitId + ";");
			rs = ps.executeQuery();

			if (rs.next()) {
				result = true;
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public void delete(Integer commitId) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `issue_commits` where `commit_id`=?;");
			ps.setInt(1, commitId);
			ps.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
	}

	public List<Integer> getIssueCommitIdListByIssue(Integer icIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select id from issue_commits where issue_id=" + icIdDB + ";");
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

	public boolean remove(Integer icId) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from issue_commits where id=" + icId + ";");
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
	
	public ArrayList<Integer> getCommitIdListByIssue(Integer issueIdDB) throws InvalidBeanException, SQLException {
		ArrayList<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select commit_id from issue_commits where issue_id=" + issueIdDB + ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt("commit_id"));
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
