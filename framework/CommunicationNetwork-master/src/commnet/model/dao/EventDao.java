package commnet.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import commnet.model.beans.Event;
import commnet.model.dao.DAOFactory.Bean;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class EventDao implements DAO<Event> {

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
	public boolean save(Event event) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `events` (`issue_id`, `contributor_id`, `is_commentary`, `created_at`) values (?,?,?,?);");
			ps.setInt(1, event.getIssueIdDB());
			ps.setInt(2, event.getCont().getIdDB());
			ps.setBoolean(3, event.isCommentary());
			if (event.getCreatedAt() == null) {
				ps.setDate(4, null);
			} else {
				ps.setString(4, getFormattedDate((event.getCreatedAt().toGMTString())));
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
	public void delete(Event object) throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Event> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public Event get(Event event) throws InvalidBeanException, SQLException {
		try {
			DeveloperNodeDao dndao = (DeveloperNodeDao) DAOFactory.getDAO(Bean.NODE);
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"select * from `events` where `id`=? or `issue_id`=? and `contributor_id`=? and `is_commentary`=? and `created_at`=?;");
			if (event.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, event.getIdDB());
			}
			ps.setInt(2, event.getIssueIdDB());
			if (event.getCont().getIdDB() == null) {
				event.getCont().setIdDB(dndao.getDevIdDB(event.getCont()));
				ps.setInt(3, event.getCont().getIdDB());
			} else {
				ps.setInt(3, event.getCont().getIdDB());
			}
			ps.setBoolean(4, event.isCommentary());
			ps.setDate(5, new Date(event.getCreatedAt().getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer issueIdDB = rs.getInt("issue_id");
				Integer contributorID = rs.getInt("contributor_id");
				boolean isCommentary = rs.getBoolean("is_commentary");
				Date createdAt = rs.getDate("created_at");
				return new Event(id, issueIdDB, contributorID, isCommentary, createdAt);
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
	public List<Event> search(Event object) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeResources() throws SQLException {
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

	public boolean remove(Integer eventId) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from events where id=" + eventId + ";");
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
