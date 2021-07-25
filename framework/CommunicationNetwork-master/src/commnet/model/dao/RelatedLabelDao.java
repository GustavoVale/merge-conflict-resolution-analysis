package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import commnet.model.dao.DAOFactory.Bean;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class RelatedLabelDao {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private Integer id;
	private Integer issueId;
	private LabelDao labelDao;

	public RelatedLabelDao() {
	}

	public RelatedLabelDao(Integer id, Integer issueId, Integer labelId) {
		setId(id);
		setIssueId(issueId);
	}

	public RelatedLabelDao(Integer id, Integer issueId, LabelDao labelDao) {
		setId(id);
		setLabelDao(labelDao);
		setIssueId(issueId);
	}

	public LabelDao getLabelDao() {
		return labelDao;
	}

	public void setLabelDao(LabelDao labelDao) {
		this.labelDao = labelDao;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIssueId() {
		return issueId;
	}

	public void setIssueId(Integer issueid) {
		this.issueId = issueid;
	}

	/**
	 * Save relatedLabel in the database
	 * 
	 * @param relatedLabel
	 * @return true if save false otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public boolean save(RelatedLabelDao relatedLabel) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `related_labels` (`issue_id`, label_id) values (?,?);");
			ps.setInt(1, relatedLabel.getIssueId());
			ps.setInt(2, relatedLabel.getLabelDao().getId());
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
	 * Get Related Label based on id or label_id and issue_id
	 * 
	 * @param relatedlabel
	 * @return RelatedLabelDao Object
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public RelatedLabelDao get(RelatedLabelDao relatedlabel) throws InvalidBeanException, SQLException {
		try {
			LabelDao labeldao = (LabelDao) DAOFactory.getDAO(Bean.LABEL);
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from related_labels where id=? or label_id=? and `issue_id`=? ;");
			if (relatedlabel.getId() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, relatedlabel.getId());
			}
			if (relatedlabel.getLabelDao().getId() == null) {
				relatedlabel.setLabelDao(labeldao.get(relatedlabel.getLabelDao()));
				ps.setInt(2, relatedlabel.getLabelDao().getId());
			} else {
				ps.setInt(2, relatedlabel.getLabelDao().getId());
			}
			ps.setInt(3, relatedlabel.getIssueId());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				Integer labelId = rs.getInt("label_id");
				Integer issueId = rs.getInt("issue_id");
				return new RelatedLabelDao(id, labelId, issueId);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public ArrayList<Integer> getByIssueId(Integer issueId) throws InvalidBeanException, SQLException {
		ArrayList<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select id from related_labels where `issue_id`=" + issueId +  ";");
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

	public boolean remove(Integer relatedlabelId) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from related_labels where id=" + relatedlabelId + ";");
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

}
