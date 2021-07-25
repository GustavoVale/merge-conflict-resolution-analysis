package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.Chunk;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ChunkDao<T> implements DAO<Chunk<?>> {

	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(Chunk<?> chunk) throws InvalidBeanException, SQLException {
		boolean hasSaved = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `chunks` ( `file_id`, `begin_line`, `end_line`, `has_conflict`) values (?,?,?,?);");
			ps.setInt(1, chunk.getFileIdDB());
			ps.setInt(2, chunk.getBeginLine());
			ps.setInt(3, chunk.getEndLine());
			ps.setBoolean(4, chunk.getIfIsConflict());
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
	public void delete(Chunk<?> chunk) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `chunks` where `id`=?;");
			ps.setInt(1, chunk.getIdDB());
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
	public List<Chunk<?>> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public Chunk<?> get(Chunk<?> chunk) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("select * from `chunks` where `id`=? or `file_id`=? and `begin_line`=?;");

			if (chunk.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, chunk.getIdDB());
			}
			ps.setInt(2, chunk.getFileIdDB());
			ps.setInt(3, chunk.getBeginLine());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer fileID = rs.getInt("file_id");
				Integer lineBegin = rs.getInt("begin_line");
				Integer lineEnd = rs.getInt("end_line");
				boolean hasConflict = rs.getBoolean("has_conflict");
				return new Chunk<T>(idDB, fileID, lineBegin, lineEnd, hasConflict);
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
	public List<Chunk<?>> search(Chunk<?> object) throws InvalidBeanException {
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

	public List<Integer> getListOfChunksInFileMS(Integer fileId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT id FROM chunks where file_id=" + fileId + ";");
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

	public Integer getNumberOfChunksInMS(Integer msId) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT count(*) FROM chunks INNER JOIN files ON chunks.file_id=files.id"
					+ " WHERE files.merge_scenarios_id=" + msId + ";");
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public Integer getNumberOfConflictedChunksInMS(Integer msId) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT count(*) FROM chunks INNER JOIN files ON chunks.file_id=files.id"
					+ " WHERE files.merge_scenarios_id=" + msId + " AND chunks.has_conflict=1;");
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<Integer> getNumberChunksbySide(Integer msId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunks.id AS chunk_id FROM commits INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON chunks.file_id=files.id "
							+ "WHERE files.merge_scenarios_id=" + msId + " AND chunk_commits.side='" + side + "';");
			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("chunk_id"))) {
					result.add(rs.getInt("chunk_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}

		return result;
	}

	public Integer getNumberConflictedChunkByFile(Integer fileId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunks.id AS chunk_id FROM commits INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id  WHERE chunks.file_id=" + fileId
							+ " AND has_conflict=1;");
			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("chunk_id"))) {
					result.add(rs.getInt("chunk_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}

		return result.size();
	}

	public Integer getNumberChunkByFileAndSide(Integer fileId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunks.id AS chunk_id FROM commits INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id  WHERE chunks.file_id=" + fileId
							+ " AND chunk_commits.side='" + side + "';");
			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("chunk_id"))) {
					result.add(rs.getInt("chunk_id"));
				}
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}

		return result.size();
	}

	public List<Integer> getChunkListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunks.id as chunk_id from chunks INNER JOIN files ON files.id=chunks.file_id"
							+ " INNER JOIN merge_scenarios ON merge_scenarios.id=files.merge_scenarios_id"
							+ " WHERE project_id=" + projectIdDB + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				result.add(rs.getInt("chunk_id"));
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
