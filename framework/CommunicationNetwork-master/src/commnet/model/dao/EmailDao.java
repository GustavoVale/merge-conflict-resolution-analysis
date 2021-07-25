package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import commnet.model.beans.DeveloperNode;
import commnet.model.dao.validators.EmailValidator;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class EmailDao implements DAO<DeveloperNode> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	@Override
	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(DeveloperNode dev) throws InvalidBeanException, SQLException {
		EmailValidator validator = new EmailValidator();
		boolean hasSaved = false;
		if (validator.validate(dev)) {
			try {
				conn = Database.getConnection();
				ps = conn.prepareStatement("insert into `email` (`contributors_id`, `email`) values (?,?);");
				ps.setInt(1, dev.getIdDB());
				ps.setString(2, dev.getEmail());
				hasSaved = ps.executeUpdate() > 0;
				conn.commit();
			} catch (SQLException e) {
				Logger.logStackTrace(e);
				conn.rollback();
			} finally {
				closeResources();
			}
			return hasSaved;
		} else {
			return false;
		}
	}

	@Override
	public void delete(DeveloperNode object) throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public DeveloperNode get(DeveloperNode dev) throws InvalidBeanException, SQLException {
		try {
			// EmailDao emaildao = (EmailDao) DAOFactory.getDAO(Bean.EMAIL);
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from email where email=?;");
			ps.setString(1, dev.getEmail());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer developer_id = rs.getInt("contributors_id");
				String email = rs.getString("email");
				return new DeveloperNode(developer_id, email);
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
	public List<DeveloperNode> search(DeveloperNode object) throws InvalidBeanException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DeveloperNode> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
