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

public class ChunkCommitDao {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	private Integer idDB;
	private Integer chunkIdDB;
	private Integer commitIdDB;
	private String side;

	public ChunkCommitDao() {
	}
	
	public ChunkCommitDao(Integer chunkId, Integer commitId, String side) {
		this(null, chunkId, commitId, side);
	}

	public ChunkCommitDao(Integer id, Integer chunkId, Integer commitId, String side) {
		setIdDB(id);
		setChunkIdDB(chunkId);
		setCommitIdDB(commitId);
		setSide(side);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getChunkIdDB() {
		return chunkIdDB;
	}

	public void setChunkIdDB(Integer chunkIdDB) {
		this.chunkIdDB = chunkIdDB;
	}

	public Integer getCommitIdDB() {
		return commitIdDB;
	}

	public void setCommitIdDB(Integer commitIdDB) {
		this.commitIdDB = commitIdDB;
	}

	public String getSide() {
		return side;
	}

	public void setSide(String side) {
		this.side = side;
	}

	/**
	 * Save commits related to a chunk in the database
	 * 
	 * @param chunkCommit
	 * @return true if it saves, false otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public boolean save(ChunkCommitDao chunkCommit) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `chunk_commits` (`chunk_id`, `commit_id`, `side`) values (?,?,?);");
			ps.setInt(1, chunkCommit.getChunkIdDB());
			ps.setInt(2, chunkCommit.getCommitIdDB());
			ps.setString(3, chunkCommit.getSide());

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
	 * Get commit related to a chunk based on its id in the database or chunk_id
	 * and commit_id and commit side
	 * 
	 * @param chunkCommit
	 * @return ChunkCommitDao Object if it finds, null otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public ChunkCommitDao get(ChunkCommitDao chunkCommit) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"select * from `chunk_commits` where `id`=? or `chunk_id`=? and `commit_id`=? and `side`=?;");

			if (chunkCommit.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, chunkCommit.getIdDB());
			}
			ps.setInt(2, chunkCommit.getChunkIdDB());
			ps.setInt(3, chunkCommit.getCommitIdDB());
			ps.setString(4, chunkCommit.getSide());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer chunkID = rs.getInt("chunk_id");
				Integer commitID = rs.getInt("commit_id");
				String side = rs.getString("side");
				return new ChunkCommitDao(idDB, chunkID, commitID, side);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return null;
	}

	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}
	
	public void delete(ChunkCommitDao chunkCommit) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `chunk_commits` where `id`=?;");
			ps.setInt(1, chunkCommit.getIdDB());
			ps.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
	}

	public List<Integer> getListOfChunkCommitInChunk(Integer chunkId) throws InvalidBeanException, SQLException{
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT id FROM chunk_commits where chunk_id=" + chunkId	+ ";");
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
	
}
