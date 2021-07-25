package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class LabelDao implements DAO<LabelDao> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private Integer id;
	private String label;

	public LabelDao() {
	}

	public LabelDao(Integer id, String label) {
		setId(id);
		setLabel(label);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(LabelDao label) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `labels` (`label`) values (?);");
			ps.setString(1, label.getLabel());
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
	public void delete(LabelDao object) throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<LabelDao> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public LabelDao get(LabelDao label) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from labels where label=?;");
			if (label.getId() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, label.getId());
			}
			ps.setString(1, label.getLabel());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				String labelName = rs.getString("label");
				return new LabelDao(id, labelName);
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
	public List<LabelDao> search(LabelDao object) throws InvalidBeanException {
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
