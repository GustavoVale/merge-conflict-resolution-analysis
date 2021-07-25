package commnet.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import commnet.model.beans.Developer;
import commnet.model.beans.DeveloperNode;
import commnet.model.dao.validators.DeveloperNodeValidator;
import commnet.model.db.Database;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class DeveloperNodeDao implements DAO<DeveloperNode> {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public boolean save(DeveloperNode node) throws InvalidBeanException, SQLException {
		DeveloperNodeValidator validator = new DeveloperNodeValidator();
		boolean hasSaved = false;
		validator.validate(node);
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("insert into `contributors` (`name`) values (?);");
			if (node.getName() == null) {
				node.setName(node.getEmail().trim().split("@")[0]);
			}
			ps.setString(1, node.getName());
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
	public void delete(DeveloperNode object) throws InvalidBeanException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<DeveloperNode> list() throws InvalidBeanException, SQLException {
		List<DeveloperNode> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from `contributors`;");
			rs = ps.executeQuery();

			while (rs.next()) {
				DeveloperNode node = new DeveloperNode();
				node.setIdDB(rs.getInt("id"));
				node.setName(rs.getString("name"));
				result.add(node);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see commnet.model.dao.DAO#get(java.lang.Object)
	 */
	@Override
	public DeveloperNode get(DeveloperNode node) throws InvalidBeanException, SQLException {
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement("select * from contributors where id=? or name=?;");
			if (node.getIdDB() == null) {
				ps.setInt(1, Integer.MAX_VALUE);
			} else {
				ps.setInt(1, node.getIdDB());
			}
			ps.setString(2, node.getName());
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt("id");
				String name = rs.getString("name");
				String email = "";
				return new DeveloperNode(id, name, email);
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
	public void closeResources() throws SQLException {
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		if (conn != null)
			conn.close();
	}

	public Integer getDevIdDB(DeveloperNode node) throws InvalidBeanException, SQLException {
		DeveloperNodeDao dndao = new DeveloperNodeDao();
		DeveloperNode aux = dndao.get(node);
		if (aux == null) {
			EmailDao emaildao = new EmailDao();
			DeveloperNode aux2 = emaildao.get(node);
			if (aux2 != null) {
				return aux2.getIdDB();
			} else {
				if (dndao.save(node)) {
					aux = dndao.get(node);
					node.setIdDB(aux.getIdDB());
					emaildao.save(node);
					return node.getIdDB();
				} else {
					throw new RuntimeException("Contributor was not saved to database!");
				}
			}
		} else {
			return aux.getIdDB();
		}
	}

	public List<Integer> getDevListByMs(Integer msId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT distinct(contributors.id) FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON chunks.file_id=files.id "
							+ "WHERE files.merge_scenarios_id=" + msId + ";");

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

	public List<Integer> getCommitterListByMs(Integer msId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT distinct(contributors.id) FROM contributors INNER JOIN commits ON commits.committer_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON chunks.file_id=files.id "
							+ "WHERE files.merge_scenarios_id=" + msId + ";");

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

	public List<Integer> getNumberDevbyMsAndSide(Integer msId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT contributors.id AS dev_id FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON chunks.file_id=files.id "
							+ "WHERE files.merge_scenarios_id=" + msId + " AND chunk_commits.side='" + side + "';");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("dev_id"))) {
					result.add(rs.getInt("dev_id"));
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

	public Integer getDevByFile(Integer fileId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT contributors.id AS dev_id FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id " + "WHERE chunks.file_id="
							+ fileId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("dev_id"))) {
					result.add(rs.getInt("dev_id"));
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

	public List<Integer> getDevByFileAndSide(Integer fileId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT contributors.id AS dev_id FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id " + "WHERE chunks.file_id="
							+ fileId + " AND chunk_commits.side='" + side + "';");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("dev_id"))) {
					result.add(rs.getInt("dev_id"));
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

	public Integer getDevByChunk(Integer chunkId) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT contributors.id AS dev_id FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "WHERE chunk_commits.chunk_id=" + chunkId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("dev_id"))) {
					result.add(rs.getInt("dev_id"));
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

	public List<Integer> getDevByChunkAndSide(Integer chunkId, String side) throws InvalidBeanException, SQLException {
		List<Integer> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT contributors.id AS dev_id FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "WHERE chunk_commits.chunk_id=" + chunkId + " AND chunk_commits.side='" + side + "';");

			rs = ps.executeQuery();
			while (rs.next()) {
				if (!result.contains(rs.getInt("dev_id"))) {
					result.add(rs.getInt("dev_id"));
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

	public Integer getNumberDevByProject(Integer projectId) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT count(distinct contributors.id) FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "WHERE commits.project_id=" + projectId + ";");

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

	public HashMap<String, Integer> getListDevByProject(Integer projectId) throws InvalidBeanException, SQLException {
		HashMap<String, Integer> result = new HashMap<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT distinct(contributors.id), contributors.name FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "WHERE commits.project_id=" + projectId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				if (name.endsWith(" ")) {
					name = name.substring(0, name.length() - 1);
				}
				name = name.toLowerCase();
				result.put(name, rs.getInt("id"));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}

		return result;
	}

	public Developer setIntegerMetricsToDeveloper(Developer developer, String side)
			throws InvalidBeanException, SQLException {

		Set<Integer> fileIdSet = new HashSet<>();
		Set<Integer> chunkIdSet = new HashSet<>();
		Set<Integer> commitIdSet = new HashSet<>();
		Integer numberOfLinesChanged = 0;
		boolean contributeToConflict = false;

		Set<Timestamp> commitDateSet = new HashSet<>();
		Timestamp newestCommitDate = null;

		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT files.id as f_id, chunks.id as ch_id, chunks.begin_line, chunks.end_line, chunks.has_conflict, "
							+ "commits.id as c_id, commit_date, contributors.* from contributors inner join commits on commits.contributor_id=contributors.id "
							+ "inner join chunk_commits on commits.id=chunk_commits.commit_id inner join chunks on chunks.id="
							+ "chunk_commits.chunk_id inner join files on files.id=chunks.file_id inner join merge_scenarios on "
							+ "merge_scenarios.id=files.merge_scenarios_id where merge_scenarios.id="
							+ developer.getMergeScenarioIdDB() + " AND contributors.id="
							+ developer.getContributorIdDB() + " AND chunk_commits.side='" + side + "';");

			rs = ps.executeQuery();

			while (rs.next()) {

				fileIdSet.add(rs.getInt("f_id"));
				chunkIdSet.add(rs.getInt("ch_id"));
				commitIdSet.add(rs.getInt("c_id"));
				if (rs.getBoolean("has_conflict")) {
					contributeToConflict = true;
					numberOfLinesChanged = numberOfLinesChanged
							+ (1 + rs.getInt("end_line") - rs.getInt("begin_line")) / 2;
				} else {
					numberOfLinesChanged = numberOfLinesChanged + 1 + rs.getInt("end_line") - rs.getInt("begin_line");
				}
				commitDateSet.add(rs.getTimestamp("commit_date"));
			}

			if (!commitDateSet.isEmpty()) {
				newestCommitDate = Collections.max(commitDateSet);
			}

			if (side.equalsIgnoreCase("left")) {
				developer.setNumberOfFilesChangedLeft(fileIdSet.size());
				developer.setNumberOfChunksChangedLeft(chunkIdSet.size());
				developer.setNumberOfLinesChangedLeft(numberOfLinesChanged);
				developer.setNumberOfCommitsLeft(commitIdSet.size());
				developer.setLastCommitLeftDate(newestCommitDate);

			} else {
				developer.setNumberOfFilesChangedRight(fileIdSet.size());
				developer.setNumberOfChunksChangedRight(chunkIdSet.size());
				developer.setNumberOfLinesChangedRight(numberOfLinesChanged);
				developer.setNumberOfCommitsRight(commitIdSet.size());
				developer.setLastCommitRightDate(newestCommitDate);
			}

			if (!developer.isContributeToConflict() && contributeToConflict) {
				developer.setContributeToConflict(true);
			}

		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}
		return developer;
	}

	public Integer getContByNameOrEmail(String author, String authorMail) throws InvalidBeanException, SQLException {
		Integer result = 0;
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT distinct(id) FROM contributors INNER JOIN email ON contributors.id=email.contributors_id "
							+ "WHERE contributors.name=\"" + author + "\" OR email=\"" + authorMail + "\";");

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

	public List<DeveloperNode> getDeveloperNodeListByMs(Integer msId) throws InvalidBeanException, SQLException {
		List<DeveloperNode> result = new ArrayList<>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT distinct(contributors.id), contributors.name FROM contributors INNER JOIN commits ON commits.contributor_id=contributors.id "
							+ "INNER JOIN chunk_commits ON commits.id=chunk_commits.commit_id "
							+ "INNER JOIN chunks ON chunk_commits.chunk_id=chunks.id INNER JOIN files ON chunks.file_id=files.id "
							+ "WHERE files.merge_scenarios_id=" + msId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(new DeveloperNode(rs.getInt("id"), rs.getString("name"), ""));
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e);
			conn.rollback();
		} finally {
			closeResources();
		}

		return result;
	}

	public List<String> getContEmailListByCommitId(Integer commitId) throws InvalidBeanException, SQLException {
		List<String> result = new ArrayList<String>();
		try {
			conn = Database.getConnection();
			ps = conn.prepareStatement(
					"SELECT email FROM commits INNER JOIN email ON commits.contributor_id=email.contributors_id "
							+ "WHERE commits.id=" + commitId + ";");

			rs = ps.executeQuery();
			while (rs.next()) {
				result.add(rs.getString("email"));
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
