package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import commnet.model.beans.Project;
import commnet.model.dao.validators.ProjectValidator;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ProjectDao implements DAO<Project> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(Project p) throws InvalidBeanException, SQLException {
		ProjectValidator validator = new ProjectValidator();
		boolean hasSaved = false;
		validator.validate(p);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `projects` (`name`, `url`) values (?,?);");
			ps.setString(1, p.getName());
			ps.setString(2, p.getUrl());
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
	public void delete(Project p) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `projects` where `id`=?;");
			ps.setInt(1, p.getIdDB());
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
	public List<Project> list() throws InvalidBeanException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public Project get(Project p) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `projects` where `url`=? or `id`=?;");
			ps.setString(1, p.getUrl());
			if (p.getIdDB() == null) {
				ps.setInt(2, Integer.MAX_VALUE);
			} else {
				ps.setInt(2, p.getIdDB());
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String url = rs.getString("url");
				return new Project(id, name, url);
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
	public List<Project> search(Project object) throws InvalidBeanException {
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

}
