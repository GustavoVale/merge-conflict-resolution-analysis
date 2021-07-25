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

public class RelatedIssueDao {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private Integer idDB;
	private Integer issueIdDB;
	private Integer relatedIssueIdDB;

	public RelatedIssueDao() {
	}

	public RelatedIssueDao(Integer id, Integer issueId, Integer relatedIssueId) {
		setIdDB(id);
		setIssueIdDB(issueId);
		setRelatedIssueIdDB(relatedIssueId);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getIssueIdDB() {
		return issueIdDB;
	}

	public void setIssueIdDB(Integer issueIdDB) {
		this.issueIdDB = issueIdDB;
	}

	public Integer getRelatedIssueIdDB() {
		return relatedIssueIdDB;
	}

	public void setRelatedIssueIdDB(Integer relatedIssueId) {
		this.relatedIssueIdDB = relatedIssueId;
	}

	/**
	 * Save relatedIssueDao in the database based on issue_id and
	 * related_issue_id
	 * 
	 * @param relatedIssue
	 * @return true if save or false otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public boolean save(RelatedIssueDao relatedIssue) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `related_issues` (`issue_id`, related_issue_id) values (?,?);");
			ps.setInt(1, relatedIssue.getIssueIdDB());
			ps.setInt(2, relatedIssue.getRelatedIssueIdDB());
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

	/**
	 * Get relatedIssue based on the database id or issue_id and
	 * related_issue_id
	 * 
	 * @param relatedIssue
	 * @return RelatedIssueDao Object
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public RelatedIssueDao get(RelatedIssueDao relatedIssue) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"select * from related_issues where id=? or issue_id=? and `related_issue_id`=? ;");
			if (relatedIssue.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, relatedIssue.getIdDB());
			}
			ps.setInt(2, relatedIssue.getIssueIdDB());
			ps.setInt(3, relatedIssue.getRelatedIssueIdDB());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer issueId = rs.getInt("issue_id");
				Integer commitId = rs.getInt("related_issue_id");
				return new RelatedIssueDao(id, issueId, commitId);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public List<Integer> getRelatedIssueIdListByIssue(Integer issueIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select id from related_issues where issue_id=" + issueIdDB + ";");
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

	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}

	public boolean remove(Integer rIssueId) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from related_issues where id=" + rIssueId + ";");
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
