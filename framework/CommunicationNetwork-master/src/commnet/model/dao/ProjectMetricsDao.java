package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import commnet.model.beans.ProjectMetrics;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ProjectMetricsDao implements DAO<ProjectMetrics> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public boolean save(ProjectMetrics projectMetrics) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `project_metrics` ( `project_id`, `loc`, `ms_computed`, `ms_ignored`,  `ms_conflicted`,"
							+ " `number_of_files`,  `number_of_chunks`, `number_of_commits`, `number_of_developers`, "
							+ " `num_of_distinct_files` ) values (?,?,?,?,?,?,?,?,?,?);");

			ps.setInt(1, projectMetrics.getProjectIdDB());
			ps.setInt(2, projectMetrics.getLoc());
			ps.setInt(3, projectMetrics.getMsComputed());
			ps.setInt(4, projectMetrics.getMsIgnored());
			ps.setInt(5, projectMetrics.getMsConflicted());
			ps.setInt(6, projectMetrics.getNumberOfFiles());
			ps.setInt(7, projectMetrics.getNumberOfChunks());
			ps.setInt(8, projectMetrics.getNumberOfCommits());
			ps.setInt(9, projectMetrics.getNumberOfDevelopers());
			ps.setInt(10, projectMetrics.getNumberOfDistinctFiles());
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
	public void delete(ProjectMetrics object) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();

			ps = conn.prepareStatement("DELETE from `project_metrics` where `id`=" + object.getIdDB() + ";");

			rs = ps.executeQuery();
			
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
	}

	@Override
	public List<ProjectMetrics> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProjectMetrics get(ProjectMetrics projectMetrics) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `project_metrics` where `id`=? or `project_id`=?;");

			if (projectMetrics.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, projectMetrics.getIdDB());
			}
			ps.setInt(2, projectMetrics.getProjectIdDB());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer networkIdDB = rs.getInt("project_id");

				return new ProjectMetrics(idDB, networkIdDB);
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
	public List<ProjectMetrics> search(ProjectMetrics object) throws InvalidBeanException {
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

	public boolean getProjectMetrics(Integer projectID) throws InvalidBeanException, SQLException {
		boolean alreadyThere = false;
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `project_metrics` where `project_id`=" + projectID + ";");

			rs = ps.executeQuery();
			if (rs.next()) {
				alreadyThere = true;
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return alreadyThere;
	}

	public boolean update(ProjectMetrics projectMetrics) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("UPDATE project_metrics SET loc=" + projectMetrics.getLoc()
					+ ", ms_computed=" + projectMetrics.getMsComputed() + ", ms_ignored="
					+ projectMetrics.getMsIgnored() + ", ms_conflicted=" + projectMetrics.getMsConflicted()
					+ ", number_of_files=" + projectMetrics.getNumberOfFiles() + ", number_of_chunks="
					+ projectMetrics.getNumberOfChunks() + ", number_of_commits=" + projectMetrics.getNumberOfCommits()
					+ ", number_of_developers=" + projectMetrics.getNumberOfDevelopers() + ", num_of_distinct_files="
					+ projectMetrics.getNumberOfDistinctFiles() + " where id=" + projectMetrics.getIdDB() + ";");
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
