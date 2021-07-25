package commnet.model.dao;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import commnet.model.beans.Chunk;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.DeveloperRole;
import commnet.model.beans.FileMS;
import commnet.model.beans.MergeScenario;
import commnet.model.dao.DAOFactory.Bean;
import commnet.model.dao.validators.MergeScenarioValidator;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class MergeScenarioDao implements DAO<MergeScenario> {

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
	public boolean save(MergeScenario mergeScenario) throws InvalidBeanException, SQLException {
		MergeScenarioValidator validator = new MergeScenarioValidator();
		boolean hasSaved = false;
		validator.validate(mergeScenario);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"insert into `merge_scenarios` ( `project_id`, `commit_base`, `commit_merge`, `has_conflict`, "
							+ "`merge_commit_date`, `base_commit_date`, `merge_commit_hash`) values (?,?,?,?,?,?,?);");
			ps.setInt(1, mergeScenario.getProjectidDB());
			ps.setInt(2, mergeScenario.getBaseCommitIdDB());
			ps.setInt(3, mergeScenario.getMergeCommitIdDB());
			ps.setBoolean(4, mergeScenario.getHasConflict());
			if (mergeScenario.getMergeDate() == null) {
				ps.setDate(5, new Date(1910, 1, 1));
			} else {
				ps.setString(5, getFormattedDate(mergeScenario.getMergeDate().toGMTString()));
			}

			if (mergeScenario.getBaseDate() == null) {
				ps.setDate(6, new Date(1910, 1, 1));
			} else {
				ps.setString(6, getFormattedDate(mergeScenario.getBaseDate().toGMTString()));
			}
			ps.setString(7, mergeScenario.getMergeCommitHash());
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
	public void delete(MergeScenario mergeScenario) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("delete from `merge_scenarios` where `id`=?;");
			ps.setInt(1, mergeScenario.getIdDB());
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
	public List<MergeScenario> list() throws InvalidBeanException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public MergeScenario get(MergeScenario mergeScenario) throws InvalidBeanException, SQLException {
		try {
			CommitDao mergeCommitDao = (CommitDao) DAOFactory.getDAO(Bean.COMMIT);
			CommitDao baseCommitDao = (CommitDao) DAOFactory.getDAO(Bean.COMMIT);
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement(
					"select * from `merge_scenarios` where `id`=? or `commit_merge`=? and `commit_base`=? and `merge_commit_hash`=?;");

			if (mergeScenario.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, mergeScenario.getIdDB());
			}
			if (mergeScenario.getMergeCommitIdDB() == null) {
				mergeScenario.setMergeCommitIdDB(mergeCommitDao.getByHash(mergeScenario.getMergeCommit().getId()));
				ps.setInt(2, mergeScenario.getMergeCommitIdDB());
			} else {
				ps.setInt(2, mergeScenario.getMergeCommitIdDB());
			}
			if (mergeScenario.getBaseCommitIdDB() == null) {
				mergeScenario.setBaseCommitIdDB(baseCommitDao.getByHash(mergeScenario.getBaseCommit().getId()));
				ps.setInt(3, mergeScenario.getBaseCommitIdDB());
			} else {
				ps.setInt(3, mergeScenario.getBaseCommitIdDB());
			}
			ps.setString(4, mergeScenario.getMergeCommitHash());

			rs = ps.executeQuery();
			if (rs.next()) {
				Integer idDB = rs.getInt("id");
				Integer projectIdDB = rs.getInt("project_id");
				Integer baseCommitID = rs.getInt("commit_base");
				Integer mergeCommitID = rs.getInt("commit_merge");
				boolean hasConflict = rs.getBoolean("has_conflict");
				return new MergeScenario(idDB, projectIdDB, baseCommitID, mergeCommitID, hasConflict);
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
	public List<MergeScenario> search(MergeScenario object) throws InvalidBeanException {
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

	public List<String> getMergeCommitsOfProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<String> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			// get with raw data
			ps = conn.prepareStatement("SELECT commits.hash FROM merge_scenarios INNER JOIN commits ON "
					+ "commits.id = merge_scenarios.commit_merge where merge_scenarios.project_id=" + projectIdDB
					+ ";");
			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("hash"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<MergeScenario> getMergeScenariosFromDataBase(Integer projectIdDB, String chunkHasConflict)
			throws InvalidBeanException, SQLException {
		List<MergeScenario> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT merge_scenarios.*, files.id AS files_id, files.filepath AS filepath, chunks.id AS chunk_id,"
							+ "chunks.begin_line, chunks.end_line, chunks.has_conflict, commits.id AS commit_id, commits.hash, chunk_commits.side,"
							+ "contributors.name, contributors.id as cont_id FROM merge_scenarios INNER JOIN files ON files.merge_scenarios_id= merge_scenarios.id"
							+ " INNER JOIN chunks ON chunks.file_id = files.id INNER JOIN chunk_commits ON chunks.id = chunk_commits.chunk_id INNER JOIN"
							+ " commits ON chunk_commits.commit_id = commits.id INNER JOIN contributors ON contributors.id = commits.contributor_id"
							+ " where merge_scenarios.project_id=" + projectIdDB + chunkHasConflict + ";");
			rs = ps.executeQuery();

			while (rs.next()) {
				MergeScenario merge = null;
				FileMS file = null;
				Chunk<Object> chunk = null;

				if (result.isEmpty()) {

					merge = new MergeScenario();
					merge.setIdDB(rs.getInt("id"));
					merge.setProjectidDB(rs.getInt("project_id"));
					merge.setBaseCommitIdDB(rs.getInt("commit_base"));
					merge.setMergeCommitIdDB(rs.getInt("commit_merge"));
					merge.setMergeDate(rs.getDate("merge_commit_date"));
					merge.setBaseDate(rs.getDate("base_commit_date"));
					merge.setHasConflict(rs.getBoolean("has_conflict"));
					merge.setMergeCommitHash(rs.getString("merge_commit_hash"));

					file = new FileMS();
					Path path = Paths.get(rs.getString("filepath"));
					file.setPath(path);
					file.setFileName(path.toString());
					file.setIdDB(rs.getInt("files_id"));
					file.setMergeScenarioIdDB(merge.getIdDB());
					file.setConflict(rs.getBoolean("has_conflict"));

					chunk = new Chunk<>();
					chunk.setIdDB(rs.getInt("chunk_id"));
					chunk.setBeginLine(rs.getInt("begin_line"));
					chunk.setEndLine(rs.getInt("end_line"));
					chunk.isConflict(rs.getBoolean("has_conflict"));
					chunk.setFileIdDB(rs.getInt("files_id"));
				} else {

					boolean mergeAlreadyExist = false;
					ListIterator<MergeScenario> listMergeIter = result.listIterator(result.size());
					while (listMergeIter.hasPrevious()) {
						MergeScenario auxMerge = listMergeIter.previous();
						if (auxMerge.getIdDB().equals(rs.getInt("id"))) {
							merge = auxMerge;
							mergeAlreadyExist = true;
							break;
						}
					}

					if (!mergeAlreadyExist) {
						merge = new MergeScenario();
						merge.setIdDB(rs.getInt("id"));
						merge.setProjectidDB(rs.getInt("project_id"));
						merge.setBaseCommitIdDB(rs.getInt("commit_base"));
						merge.setMergeCommitIdDB(rs.getInt("commit_merge"));
						merge.setMergeDate(rs.getDate("merge_commit_date"));
						merge.setBaseDate(rs.getDate("base_commit_date"));
						merge.setHasConflict(rs.getBoolean("has_conflict"));
						merge.setMergeCommitHash(rs.getString("merge_commit_hash"));

					}

					boolean fileAlreadyExist = false;
					ListIterator<FileMS> listFileIter = merge.getListFileMS()
							.listIterator(merge.getListFileMS().size());
					while (listFileIter.hasPrevious()) {
						FileMS auxFile = listFileIter.previous();
						if (auxFile.getIdDB().equals(rs.getInt("files_id"))) {
							file = auxFile;
							fileAlreadyExist = true;
							break;
						}
					}
					if (!fileAlreadyExist) {
						file = new FileMS();
						Path path = Paths.get(rs.getString("filepath"));
						file.setPath(path);
						file.setFileName(path.toString());
						file.setIdDB(rs.getInt("files_id"));
						file.setMergeScenarioIdDB(merge.getIdDB());
						file.setConflict(rs.getBoolean("has_conflict"));
					}

					boolean chunkAlreadyExist = false;
					ListIterator<Chunk<Object>> listChunkIter = file.getChunkList()
							.listIterator(file.getChunkList().size());
					while (listChunkIter.hasPrevious()) {
						Chunk<Object> auxChunk = listChunkIter.previous();
						if (auxChunk.getIdDB().equals(rs.getInt("chunk_id"))) {
							chunk = auxChunk;
							chunkAlreadyExist = true;
							break;
						}
					}

					if (!chunkAlreadyExist) {
						chunk = new Chunk<>();
						chunk.setIdDB(rs.getInt("chunk_id"));
						chunk.setBeginLine(rs.getInt("begin_line"));
						chunk.setEndLine(rs.getInt("end_line"));
						chunk.isConflict(rs.getBoolean("has_conflict"));
						chunk.setFileIdDB(rs.getInt("files_id"));
					}
				}

				DeveloperNode dev = new DeveloperNode();
				dev.setIdDB(rs.getInt("cont_id"));
				dev.setName(rs.getString("name"));

				String side = rs.getString("side");
				if (side.equals("left")) {
					if (!chunk.getLeftDevList().contains(dev)) {
						chunk.getLeftDevList().add(dev);
					}
				} else {
					if (!chunk.getRightDevList().contains(dev)) {
						chunk.getRightDevList().add(dev);
					}
				}

				if (!file.getChunkList().contains(chunk)) {
					file.getChunkList().add(chunk);
				}

				if (!merge.getListFileMS().contains(file)) {
					merge.getListFileMS().add(file);
				}

				if (!result.contains(merge)) {
					result.add(merge);
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

	public boolean isThereMSByMergeCommitId(Integer mergeCommitId) throws InvalidBeanException, SQLException {
		boolean isMSThere = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT * FROM merge_scenarios where commit_merge=" + mergeCommitId + ";");
			rs = ps.executeQuery();
			if (rs.next()) {
				isMSThere = true;
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return isMSThere;
	}

	public Integer getMsIdByMergeCommitId(Integer mergeCommitId) throws InvalidBeanException, SQLException {
		Integer result = null;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT id FROM merge_scenarios where commit_merge=" + mergeCommitId + ";");
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getInt("id");
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<Integer> getMSListByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT * from merge_scenarios WHERE project_id=" + projectIdDB + ";");

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

	public Integer getConflictedMSByProject(Integer projectIdDB) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT count(*) from merge_scenarios WHERE project_id=" + projectIdDB + " AND has_conflict=1;");
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

	public List<Chunk<?>> getListOfChunksFromOneMergeScenario(Integer msId) throws InvalidBeanException, SQLException {
		List<Chunk<?>> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunks.* from chunks inner join files on chunks.file_id=files.id inner join merge_scenarios on "
							+ "files.merge_scenarios_id=merge_scenarios.id WHERE merge_scenarios.id=" + msId + ";");

			rs = ps.executeQuery();

			while (rs.next()) {
				Integer chunkIdDB = rs.getInt("id");
				Integer fileIdDB = rs.getInt("file_id");
				Integer beginLine = rs.getInt("begin_line");
				Integer endLine = rs.getInt("end_line");
				boolean hasConflict = rs.getBoolean("has_conflict");
				Chunk<?> newChunk = new Chunk(chunkIdDB, fileIdDB, beginLine, endLine, hasConflict);
				result.add(newChunk);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}
	
	public List<Chunk<?>> getListOfConflictingChunksFromOneMergeScenario(Integer msId) throws InvalidBeanException, SQLException {
		List<Chunk<?>> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT chunks.* from chunks inner join files on chunks.file_id=files.id inner join merge_scenarios on "
							+ "files.merge_scenarios_id=merge_scenarios.id WHERE merge_scenarios.id=" + msId + " and chunks.has_conflict=1;");

			rs = ps.executeQuery();

			while (rs.next()) {
				Integer chunkIdDB = rs.getInt("id");
				Integer fileIdDB = rs.getInt("file_id");
				Integer beginLine = rs.getInt("begin_line");
				Integer endLine = rs.getInt("end_line");
				boolean hasConflict = rs.getBoolean("has_conflict");
				Chunk<?> newChunk = new Chunk(chunkIdDB, fileIdDB, beginLine, endLine, hasConflict);
				result.add(newChunk);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public MergeScenario getMergeScenarioByHashFromDataBase(String commitHash)
			throws InvalidBeanException, SQLException {
		MergeScenario merge = new MergeScenario();
		boolean mergeAlreadyExist = false;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT merge_scenarios.*, files.id AS files_id, files.filepath AS filepath, chunks.id AS chunk_id,"
							+ "chunks.begin_line, chunks.end_line, chunks.has_conflict, commits.id AS commit_id, commits.hash, chunk_commits.side,"
							+ "contributors.name, contributors.id as cont_id FROM merge_scenarios INNER JOIN files ON files.merge_scenarios_id= merge_scenarios.id"
							+ " INNER JOIN chunks ON chunks.file_id = files.id INNER JOIN chunk_commits ON chunks.id = chunk_commits.chunk_id INNER JOIN"
							+ " commits ON chunk_commits.commit_id = commits.id INNER JOIN contributors ON contributors.id = commits.contributor_id"
							+ " where merge_scenarios.merge_commit_hash=\"" + commitHash + "\";");
			rs = ps.executeQuery();

			while (rs.next()) {
				FileMS file = null;
				Chunk<Object> chunk = null;

				if (!mergeAlreadyExist) {

					merge = new MergeScenario();
					merge.setIdDB(rs.getInt("id"));
					merge.setProjectidDB(rs.getInt("project_id"));
					merge.setBaseCommitIdDB(rs.getInt("commit_base"));
					merge.setMergeCommitIdDB(rs.getInt("commit_merge"));
					merge.setMergeDate(rs.getDate("merge_commit_date"));
					merge.setBaseDate(rs.getDate("base_commit_date"));
					merge.setHasConflict(rs.getBoolean("has_conflict"));
					merge.setMergeCommitHash(rs.getString("merge_commit_hash"));

					file = new FileMS();
					Path path = Paths.get(rs.getString("filepath"));
					file.setPath(path);
					file.setFileName(path.toString());
					file.setIdDB(rs.getInt("files_id"));
					file.setMergeScenarioIdDB(merge.getIdDB());
					file.setConflict(rs.getBoolean("has_conflict"));

					chunk = new Chunk<>();
					chunk.setIdDB(rs.getInt("chunk_id"));
					chunk.setBeginLine(rs.getInt("begin_line"));
					chunk.setEndLine(rs.getInt("end_line"));
					chunk.isConflict(rs.getBoolean("has_conflict"));
					chunk.setFileIdDB(rs.getInt("files_id"));

					mergeAlreadyExist = true;
				} else {

					boolean fileAlreadyExist = false;
					ListIterator<FileMS> listFileIter = merge.getListFileMS()
							.listIterator(merge.getListFileMS().size());
					while (listFileIter.hasPrevious()) {
						FileMS auxFile = listFileIter.previous();
						if (auxFile.getIdDB().equals(rs.getInt("files_id"))) {
							file = auxFile;
							fileAlreadyExist = true;
							break;
						}
					}
					if (!fileAlreadyExist) {
						file = new FileMS();
						Path path = Paths.get(rs.getString("filepath"));
						file.setPath(path);
						file.setFileName(path.toString());
						file.setIdDB(rs.getInt("files_id"));
						file.setMergeScenarioIdDB(merge.getIdDB());
						file.setConflict(rs.getBoolean("has_conflict"));
					}

					boolean chunkAlreadyExist = false;
					ListIterator<Chunk<Object>> listChunkIter = file.getChunkList()
							.listIterator(file.getChunkList().size());
					while (listChunkIter.hasPrevious()) {
						Chunk<Object> auxChunk = listChunkIter.previous();
						if (auxChunk.getIdDB().equals(rs.getInt("chunk_id"))) {
							chunk = auxChunk;
							chunkAlreadyExist = true;
							break;
						}
					}

					if (!chunkAlreadyExist) {
						chunk = new Chunk<>();
						chunk.setIdDB(rs.getInt("chunk_id"));
						chunk.setBeginLine(rs.getInt("begin_line"));
						chunk.setEndLine(rs.getInt("end_line"));
						chunk.isConflict(rs.getBoolean("has_conflict"));
						chunk.setFileIdDB(rs.getInt("files_id"));
					}
				}

				DeveloperNode dev = new DeveloperNode();
				dev.setIdDB(rs.getInt("cont_id"));
				dev.setName(rs.getString("name"));

				String side = rs.getString("side");
				if (side.equals("left")) {
					if (!chunk.getLeftDevList().contains(dev)) {
						chunk.getLeftDevList().add(dev);
					}
				} else {
					if (!chunk.getRightDevList().contains(dev)) {
						chunk.getRightDevList().add(dev);
					}
				}

				if (!file.getChunkList().contains(chunk)) {
					file.getChunkList().add(chunk);
				}

				if (!merge.getListFileMS().contains(file)) {
					merge.getListFileMS().add(file);
				}

			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return merge;

	}

	public Integer getIntegratorIdDBByMergeScenario(Integer mergeScenarioId) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("SELECT commits.contributor_id from merge_scenarios inner join commits on "
					+ "merge_scenarios.commit_merge=commits.id WHERE merge_scenarios.id=" + mergeScenarioId + ";");
			rs = ps.executeQuery();

			if (rs.next()) {
				result = rs.getInt("contributor_id");
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	public List<DeveloperRole> getIntegratorListByProject(Integer projectId) throws InvalidBeanException, SQLException {
		List<DeveloperRole> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT id, commit_merge FROM merge_scenarios where project_id=" + projectId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(new DeveloperRole(null, rs.getInt("id"), rs.getInt("commit_merge")));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}

		return result;
	}

	public String getMergeCommitHash(Integer mergeScenarioId) throws InvalidBeanException, SQLException {
		String result = "";
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT merge_scenarios.merge_commit_hash from merge_scenarios WHERE id=" + mergeScenarioId + ";");
			rs = ps.executeQuery();

			if (rs.next()) {
				result = rs.getString("merge_commit_hash");
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
