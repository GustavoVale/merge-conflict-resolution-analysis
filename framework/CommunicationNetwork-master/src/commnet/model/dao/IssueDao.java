package commnet.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import commnet.model.beans.DeveloperNode;
import commnet.model.beans.Event;
import commnet.model.beans.Issue;
import commnet.model.dao.validators.IssueValidator;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

/*
 * REVIEW ALL THIS CLASS
 */

public class IssueDao implements DAO<Issue> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean save(Issue issue) throws InvalidBeanException, SQLException {
		IssueValidator validator = new IssueValidator();
		boolean hasSaved = false;
		validator.validate(issue);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `issues` ( `project_id`, `gh_issue_id`, `open_date`, `close_date`, `is_pull_request`, `is_closed`, `pull_commit_hash`) values (?,?,?,?,?,?,?);");
			if (issue.getProjectID() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, issue.getProjectID());
			}
			ps.setInt(2, issue.getIssueID());
			if (issue.getOpenedDate() == null) {
				ps.setDate(3, new Date(1900, 1, 1));
			} else {
				ps.setString(3, getFormattedDate(issue.getOpenedDate().toGMTString()));
			}

			if (issue.getClosedDate() == null) {
				ps.setDate(4, null);
			} else {
				ps.setString(4, getFormattedDate(issue.getClosedDate().toGMTString()));
			}

			ps.setBoolean(5, issue.getIsPullRequest());
			ps.setBoolean(6, issue.getIsClosed());

			if (issue.getPullHash() == null) {
				ps.setString(7, null);
			} else {
				ps.setString(7, issue.getPullHash());
			}

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
	public void delete(Issue issue) throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Issue> list() throws InvalidBeanException {
		return list(null);
	}

	public List<Issue> list(Integer projectID) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public Issue get(Issue issue) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `issues` where `id`=? or `project_id`=? and `gh_issue_id`=?;");

			if (issue.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, issue.getIdDB());
			}
			ps.setInt(2, issue.getProjectID());
			ps.setInt(3, issue.getIssueID());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer projectID = rs.getInt("project_id");
				Integer issueId = rs.getInt("gh_issue_id");
				Date createdAt = new Date(rs.getDate("open_date").getTime());
				Date closedAt = null;
				Optional<Date> optional = Optional.ofNullable(rs.getDate("close_date"));
				if (optional.isPresent()) {
					closedAt = new Date(rs.getDate("close_date").getTime());
				}

				boolean isPullRequest = rs.getBoolean("is_pull_request");
				boolean isClosed = rs.getBoolean("is_closed");
				return (new Issue(id, projectID, issueId, createdAt, closedAt, isPullRequest, isClosed));
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
	public List<Issue> search(Issue object) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResources() {
		try {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			Logger.logStackTrace(e);
		}
	}

	public List<Issue> getIssuesFromDataBase(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Issue> result = new ArrayList<>();

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT * FROM issues where project_id=" + projectIdDB + ";");
			rs = ps.executeQuery();

			while (rs.next()) {

				Issue issue = new Issue();
				issue.setIdDB(rs.getInt("id"));
				issue.setProjectID(rs.getInt("project_id"));
				issue.setIssueID(rs.getInt("gh_issue_id"));
				issue.setOpenedDate(rs.getDate("open_date"));
				issue.setClosedDate(rs.getDate("close_date"));
				issue.setIsPullRequest(rs.getBoolean("is_pull_request"));
				issue.setIsClosed(rs.getBoolean("is_closed"));
				if (rs.getString("pull_commit_hash") != null) {
					issue.setPullHash(rs.getString("pull_commit_hash"));
				}

				issue.setRelatedEvents(getIssuesEventsFromDataBase(issue.getIdDB()));
				issue.setRelatedIssues(getRelatedIssuesFromDataBase(issue.getIdDB()));
				issue.setDevListByEvents();

				result.add(issue);
			}
		} catch (

		SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	private List<Event> getIssuesEventsFromDataBase(Integer issueIdDB) throws InvalidBeanException, SQLException {
		List<Event> eventList = new ArrayList<>();
		List<DeveloperNode> devList = new ArrayList<>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT events.*, contributors.name, issues.gh_issue_id AS github_issue_id "
					+ "FROM events INNER JOIN contributors ON contributors.id = events.contributor_id "
					+ "INNER JOIN issues ON issues.id = events.issue_id where events.issue_id=" + issueIdDB + ";");
			rs = ps.executeQuery();

			while (rs.next()) {

				DeveloperNode dev = null;

				boolean devAlreadyExist = false;
				for (DeveloperNode auxDev : devList) {
					if (auxDev.getIdDB().equals(rs.getInt("contributor_id"))) {
						dev = auxDev;
						devAlreadyExist = true;
						break;
					}
				}

				if (!devAlreadyExist) {
					dev = new DeveloperNode();
					dev.setIdDB(rs.getInt("contributor_id"));
					dev.setName(rs.getString("name"));
					devList.add(dev);
				}

				Event event = new Event();
				event.setIdDB(rs.getInt("id"));
				event.setIssueIdDB(rs.getInt("issue_id"));
				event.setIssueID(rs.getInt("github_issue_id"));
				event.setCommentary(rs.getBoolean("is_commentary"));
				event.setCreatedAt(rs.getDate("created_at"));
				event.setCont(dev);

				eventList.add(event);

			}
		} catch (

		SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (conn != null)
				conn.close();
		}
		return eventList;
	}

	private List<Integer> getRelatedIssuesFromDataBase(Integer issueIdDB) throws InvalidBeanException, SQLException {
		List<Integer> auxIssuesList = new ArrayList<>();
		List<Integer> relatedIssuesList = new ArrayList<>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT * FROM related_issues where issue_id=" + issueIdDB + ";");
			rs = ps.executeQuery();

			while (rs.next()) {
				Integer relatedIssueId = rs.getInt("related_issue_id");
				auxIssuesList.add(relatedIssueId);
			}
		} catch (

		SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
			if (conn != null)
				conn.close();
		}

		for (Integer id : auxIssuesList) {
			try {
				conn = Database.getConnection();
				ps = conn.prepareStatement("SELECT issues.gh_issue_id FROM issues where id=" + id + ";");
				rs = ps.executeQuery();

				while (rs.next()) {
					Integer relatedIssueId = rs.getInt("gh_issue_id");
					relatedIssuesList.add(relatedIssueId);
				}
			} catch (

			SQLException e) {
				Logger.logStackTrace(e);
				conn.rollback();
			} finally {
				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
				if (conn != null)
					conn.close();
			}
		}
		return relatedIssuesList;
	}

	public List<Integer> getGitHubIssuesID(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT issues.gh_issue_id FROM issues where project_id=" + projectIdDB + " AND is_closed=1;");
			rs = ps.executeQuery();

			while (rs.next()) {

				result.add(rs.getInt("gh_issue_id"));
			}

		} catch (

		SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public boolean update(Issue issue) throws InvalidBeanException, SQLException {
		boolean hasUpdated = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("UPDATE issues SET close_date=?, is_closed=?, pull_commit_hash=? WHERE id="
					+ issue.getIdDB() + ";");

			if (issue.getClosedDate() == null) {
				ps.setDate(1, null);
			} else {
				ps.setDate(1, new Date(issue.getClosedDate().getTime()));
			}
			ps.setBoolean(2, issue.getIsClosed());

			if (issue.getPullHash() == null) {
				ps.setString(3, null);
			} else {
				ps.setString(3, issue.getPullHash());
			}

			hasUpdated = ps.executeUpdate() > 0;
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
		return hasUpdated;
	}

	public boolean remove(Integer issueIdDB) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from issues where id=" + issueIdDB + ";");
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
