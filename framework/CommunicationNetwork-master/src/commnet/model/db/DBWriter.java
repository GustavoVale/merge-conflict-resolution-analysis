package commnet.model.db;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import gitwrapper.repo.Commit;
import commnet.model.beans.Chunk;
import commnet.model.beans.ChunkMetrics;
import commnet.model.beans.CommitN;
import commnet.model.beans.Developer;
import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.DeveloperRole;
import commnet.model.beans.Event;
import commnet.model.beans.FileMS;
import commnet.model.beans.FileMetrics;
import commnet.model.beans.Issue;
import commnet.model.beans.MergeConflictInfo;
import commnet.model.beans.MergeConflictMetrics;
import commnet.model.beans.MergeScenario;
import commnet.model.beans.MergeScenarioMetrics;
import commnet.model.beans.Network;
import commnet.model.beans.NetworkMetrics;
import commnet.model.beans.Project;
import commnet.model.beans.ProjectMetrics;
import commnet.model.dao.ChunkCommitDao;
import commnet.model.dao.ChunkDao;
import commnet.model.dao.ChunkMetricDao;
import commnet.model.dao.CommitDao;
import commnet.model.dao.DeveloperEdgeDao;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.DeveloperRoleDao;
import commnet.model.dao.EmailDao;
import commnet.model.dao.EventDao;
import commnet.model.dao.FileDao;
import commnet.model.dao.FileMetricDao;
import commnet.model.dao.IssueCommitDao;
import commnet.model.dao.IssueDao;
import commnet.model.dao.LabelDao;
import commnet.model.dao.MergeConflictInfoDao;
import commnet.model.dao.MergeConflictMetricsDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.dao.MergeScenarioMetricDao;
import commnet.model.dao.NetworkDao;
import commnet.model.dao.NetworkMetricDao;
import commnet.model.dao.ProjectDao;
import commnet.model.dao.ProjectMetricsDao;
import commnet.model.dao.RelatedIssueDao;
import commnet.model.dao.RelatedLabelDao;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public enum DBWriter {

	INSTANCE;

	private ProjectDao pdao;
	private CommitDao commitdao;
	private NetworkDao ndao;
	private DeveloperEdgeDao dedao;
	private DeveloperNodeDao dndao;
	private EmailDao emaildao;
	private MergeScenarioDao msdao;
	private MergeConflictInfoDao mciDao;
	private FileDao fdao;
	private ChunkDao<?> cdao;
	private ChunkCommitDao ccdao;
	private IssueDao issuedao;
	private IssueCommitDao icdao;
	private LabelDao labeldao;
	private RelatedLabelDao rldao;
	private EventDao eventdao;
	private RelatedIssueDao ridao;
	private ProjectMetricsDao pmdao;
	private NetworkMetricDao nmdao;
	private MergeScenarioMetricDao msmdao;
	private FileMetricDao fmdao;
	private ChunkMetricDao chmdao;
	private DeveloperRoleDao devRoleDao;
	private MergeConflictMetricsDao mcmdao;

	private File log;

	private String edgeRowList = "";
	private String netMetricRowList = "";
	private String msMetricRowList = "";
	private String fileMetricRowList = "";
	private String chunkMetricRowList = "";
	private String commitRowList = "";
	private String devRowList = "";
	private String MergeConflictMetricRowString = "";

	/**
	 * Set log
	 * 
	 * @param log
	 */
	public void setLogFile(File log) {
		this.log = log;
	}

	/**
	 * Persist all the commits of a project in the database
	 * 
	 * @param commit
	 * @return commit with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized CommitN persistCommit(CommitN commit) throws InvalidBeanException, SQLException {
		commitdao = new CommitDao();
		CommitN aux = commitdao.get(commit);
		if (aux == null) {
			if (commitdao.save(commit)) {
				aux = commitdao.get(commit);
			} else {
				throw new RuntimeException("Commit was not saved to database!");
			}
		}
		commit.setIdDB(aux.getIdDB());
		return commit;
	}

	/**
	 * Persist project in the database
	 * 
	 * @param project
	 * @return - the project with the updated IdDB.
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public synchronized Project persistProject(Project project) throws InvalidBeanException, SQLException {
		pdao = new ProjectDao();
		Project aux = pdao.get(project);
		if (aux == null) {
			if (pdao.save(project)) {
				aux = pdao.get(project);
			} else {
				throw new RuntimeException("Project was not saved to database!");
			}
		}
		project.setIdDB(aux.getIdDB());
		return project;
	}

	/**
	 * Persist network in the database
	 * 
	 * @param contNet
	 * @return network with the IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized Network persistContNet(Network contNet) throws InvalidBeanException, SQLException {
		ndao = new NetworkDao();
		Network aux = ndao.get(contNet);
		if (aux == null) {
			if (ndao.save(contNet)) {
				aux = ndao.get(contNet);
			} else {
				throw new RuntimeException("Contribution Network was not saved to database!");
			}
		}
		contNet.setIdDB(aux.getIdDB());
		return contNet;
	}

	/**
	 * Get the parameters of a edge to create a larger query
	 * 
	 * @param edge
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized void gettingLargerQueryForDeveloperEdge(DeveloperEdge edge)
			throws InvalidBeanException, SQLException {
		if (edgeRowList.isEmpty()) {
			edgeRowList = ("insert into `edges` (`network_id`, `dev_a`, `dev_b`, `type`, `side`, `weight`) values");
		}
		edgeRowList = edgeRowList + getNewEdgeRow(edge);
	}

	/**
	 * Create a String with the value of edges table (without id)
	 * 
	 * @param edge
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private String getNewEdgeRow(DeveloperEdge edge) {
		String space = ", ";
		String result = " (" + edge.getNetworkID() + space + edge.getDevA().getIdDB() + space + edge.getDevB().getIdDB()
				+ space + edge.getEdgeType() + space + "'" + edge.getEdgeSide() + "'" + space + edge.getWeight() + "),";

		return result;

	}

	/**
	 * save multiple (developerEdges) edges in the database
	 * 
	 * @param edge
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private void savingEdgeRows() throws InvalidBeanException, SQLException {
		dedao = new DeveloperEdgeDao();
		edgeRowList = edgeRowList.substring(0, edgeRowList.length() - 1);
		edgeRowList = edgeRowList + ";";

		if (!dedao.saveManyRows(edgeRowList)) {
			throw new RuntimeException("Edges could not be stored to database!");
		}
		edgeRowList = "";
	}

	/**
	 * Persists a node in the database and update the parameter with the its
	 * respective ID.
	 * 
	 * @param node
	 * @return DeveloperNode with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public synchronized DeveloperNode persistNode(DeveloperNode node) throws InvalidBeanException, SQLException {
		// save developers
		dndao = new DeveloperNodeDao();
		DeveloperNode aux = dndao.get(node);
		emaildao = new EmailDao();
		DeveloperNode aux2 = emaildao.get(node);
		if (aux == null && aux2 == null) {
			if (dndao.save(node)) {
				aux = dndao.get(node);
				node.setIdDB(aux.getIdDB());
				emaildao.save(node);
				return node;
			} else {
				throw new RuntimeException("Contributor was not saved to database!");
			}
		} else if (aux == null) {
			node.setIdDB(aux2.getIdDB());
			return node;
		} else if (aux2 == null) {
			node.setIdDB(aux.getIdDB());
			emaildao.save(node);
		}
		node.setIdDB(aux.getIdDB());
		return node;
	}

	/**
	 * Persist merge Scenarios in the database
	 * 
	 * @param mergeScenario
	 * @return merge scenario with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized MergeScenario persistMergeScenario(MergeScenario mergeScenario)
			throws InvalidBeanException, SQLException {
		msdao = new MergeScenarioDao();
		MergeScenario aux = msdao.get(mergeScenario);
		if (aux == null) {
			// Item not in database
			if (msdao.save(mergeScenario)) {
				aux = msdao.get(mergeScenario);
			} else {
				throw new RuntimeException("MergeScenario was not saved to database!");
			}
		}
		mergeScenario.setIdDB(aux.getIdDB());
		return mergeScenario;
	}

	/**
	 * Persists file in the database
	 * 
	 * @param file
	 * @return file with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized FileMS persistFile(FileMS file) throws InvalidBeanException, SQLException {
		fdao = new FileDao();
		FileMS aux = new FileMS();
		// Item not in database
		if (fdao.save(file)) {
			aux = fdao.get(file);
		} else {
			throw new RuntimeException("File was not saved to database!");
		}
		if (aux.getIdDB() != null) {
			file.setIdDB(aux.getIdDB());
		}
		return file;
	}

	/**
	 * Persist chunk in the data base
	 * 
	 * @param chunk
	 * @return chunk with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized Chunk<?> persistChunk(Chunk<?> chunk) throws InvalidBeanException, SQLException {
		cdao = new ChunkDao<Object>();
		Chunk<?> aux = new Chunk<>();
		// Item not in database
		if (cdao.save(chunk)) {
			aux = cdao.get(chunk);
		} else {
			throw new RuntimeException("Chunk was not saved to database!");
		}
		if (aux.getIdDB() != null) {
			chunk.setIdDB(aux.getIdDB());
		}
		return chunk;
	}

	/**
	 * Persist commit in the database
	 * 
	 * @param chunkCommit
	 * @return commitDao with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized ChunkCommitDao persistChunkCommit(ChunkCommitDao chunkCommit)
			throws InvalidBeanException, SQLException {
		ccdao = new ChunkCommitDao();
		ChunkCommitDao aux = ccdao.get(chunkCommit);
		if (aux == null) {
			// Item not in database
			if (ccdao.save(chunkCommit)) {
				aux = ccdao.get(chunkCommit);
			} else {
				throw new RuntimeException("A commits related to Chunk was not saved to database!");
			}
		}
		chunkCommit.setIdDB(aux.getIdDB());
		return chunkCommit;
	}

	/**
	 * Persist issue in the database
	 * 
	 * @param issue
	 * @return issue with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized Issue persistIssue(Issue issue) throws InvalidBeanException, SQLException {
		issuedao = new IssueDao();
		Issue aux = issuedao.get(issue);
		if (aux == null) {
			// Item not in database
			if (issuedao.save(issue)) {
				aux = issuedao.get(issue);
			} else {
				throw new RuntimeException("Issue was not saved to database!");
			}
		} else if (!aux.getIsClosed() && aux.getClosedDate() != issue.getClosedDate()) {
			issue.setIdDB(aux.getIdDB());
			if (!issuedao.update(issue)) {
				throw new RuntimeException("Issue was not updated to database!");
			}
		}
		issue.setIdDB(aux.getIdDB());
		return issue;
	}

	/**
	 * Persist Label in the database
	 * 
	 * @param label
	 * @return label with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized LabelDao persistLabel(LabelDao label) throws InvalidBeanException, SQLException {
		labeldao = new LabelDao();
		LabelDao aux = labeldao.get(label);
		if (aux == null) {
			// Item not in database
			if (labeldao.save(label)) {
				aux = labeldao.get(label);
			} else {
				throw new RuntimeException("Label was not saved to database!");
			}
		}
		label.setId(aux.getId());
		return label;
	}

	/**
	 * Persist commits realted to issues in the database
	 * 
	 * @param relatedCommit
	 * @return issueCommitDao with the IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized void persistIssueCommit(IssueCommitDao relatedCommit)
			throws InvalidBeanException, SQLException {
		icdao = new IssueCommitDao();
		IssueCommitDao aux = icdao.get(relatedCommit);
		if (aux == null) {
			// Item not in database
			if (!icdao.save(relatedCommit)) {
				throw new RuntimeException("Related Commit was not saved to database!");
			}
		}
	}

	/**
	 * Persist labels related to an Issue in the database
	 * 
	 * @param relatedLabel
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized void persistRelatedLabel(RelatedLabelDao relatedLabel)
			throws InvalidBeanException, SQLException {
		rldao = new RelatedLabelDao();
		RelatedLabelDao aux = rldao.get(relatedLabel);
		if (aux == null) {
			// Item not in database
			if (!rldao.save(relatedLabel)) {
				throw new RuntimeException("RelatedIssue was not saved to database!");
			}
		}
	}

	/**
	 * Persist events related to an issue in the database
	 * 
	 * @param event
	 * @return event with IdDB
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized void persistEvent(Event event) throws InvalidBeanException, SQLException {
		eventdao = new EventDao();
		Event aux = eventdao.get(event);
		if (aux == null) {
			// Item not in database
			if (!eventdao.save(event)) {
				throw new RuntimeException("Event was not saved to database!");
			}
		}
	}

	/**
	 * Persist issue from related issue in the database
	 *
	 * @param issue
	 * @return
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized Issue persistIssueFromRelatedIssue(Issue issue) throws InvalidBeanException, SQLException {
		issuedao = new IssueDao();
		Issue aux = issuedao.get(issue);
		if (aux == null) {
			Logger.log(log, "Issue was removed or it is wrong");
		}
		return aux;
	}

	/**
	 * Persist issues related to other issue in the database
	 * 
	 * @param relatedissueId
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	private synchronized void persistRelatedIssue(RelatedIssueDao relatedissueId)
			throws InvalidBeanException, SQLException {
		ridao = new RelatedIssueDao();
		RelatedIssueDao aux = ridao.get(relatedissueId);
		if (aux == null) {
			// Item not in database
			if (!ridao.save(relatedissueId)) {
				throw new RuntimeException("RelatedIssue was not saved to database!");
			}
		}
	}

	/**
	 * Persist merge scenarios in the database and related tables to the database
	 * 
	 * @param projectIdDB
	 * @param mergeScenario
	 * @throws SQLException
	 */
	public synchronized void persistMergeScenarioDB(Integer projectIdDB, MergeScenario mergeScenario)
			throws SQLException {
		try {

			for (Commit commit : mergeScenario.getListCommitMergeScenario()) {
				if (commit == null) {
					continue;
				}
				DeveloperNode newDev = new DeveloperNode(null, commit.getAuthor(), commit.getAuthorMail());
				newDev = persistNode(newDev);
				DeveloperNode newCommitter = new DeveloperNode(null, commit.getCommitter(), commit.getCommitterMail());
				newCommitter = persistNode(newCommitter);
				CommitN newCommit = new CommitN(null, projectIdDB, newDev.getIdDB(), newCommitter.getIdDB(),
						commit.getId(), commit.getAuthorTime());
				newCommit = persistCommit(newCommit);

			}

			mergeScenario = persistMergeScenario(mergeScenario);

			Integer contIdDB = commitdao.getByIdDB(mergeScenario.getMergeCommitIdDB()).getContIdDB();
			DeveloperRole newIntegrator = new DeveloperRole(null, mergeScenario.getIdDB(), contIdDB);
			persistDeveloperRole(newIntegrator, "integrators");

			// Setting files of this (mergeScenairo)
			for (FileMS file : mergeScenario.getListFileMS()) {
				file.setMergeScenarioIdDB(mergeScenario.getIdDB());
				file = persistFile(file);
				// Setting chunks of this (file)
				for (Chunk<?> chunk : file.getChunkList()) {
					chunk.setFileIdDB(file.getIdDB());
					chunk = persistChunk(chunk);
					for (Commit leftCommit : chunk.getLeftCommitList()) {
						CommitN newCommit = new CommitN(null, projectIdDB, null, null, leftCommit.getId(),
								leftCommit.getAuthorTime());
						newCommit = persistCommit(newCommit);
						ChunkCommitDao relatedChunkCommit = new ChunkCommitDao(chunk.getIdDB(), newCommit.getIdDB(),
								"left");
						relatedChunkCommit = persistChunkCommit(relatedChunkCommit);
					}
					for (Commit rightCommit : chunk.getRightCommitList()) {
						CommitN newCommit = new CommitN(null, projectIdDB, null, null, rightCommit.getId(),
								rightCommit.getAuthorTime());
						newCommit = persistCommit(newCommit);
						ChunkCommitDao relatedChunkCommit = new ChunkCommitDao(chunk.getIdDB(), newCommit.getIdDB(),
								"right");
						relatedChunkCommit = persistChunkCommit(relatedChunkCommit);
					}
				}
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Persist MergeConflictInfo table in the database
	 * 
	 * @param mergeConflictInfo
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public synchronized void persistMergeConflictCodeDB(MergeConflictInfo mergeConflictInfo)
			throws InvalidBeanException, SQLException {
		mciDao = new MergeConflictInfoDao();
		MergeConflictInfo aux = mciDao.get(mergeConflictInfo);
		if (aux == null) {
			// Item not in database
			if (!mciDao.save(mergeConflictInfo)) {
				// If the mergeConFlictInfo instance is not saved, we assumed that it was
				// because of the size of the code

				if (mciDao.saveWithoutCode(mergeConflictInfo)) {
					// After the mergeConflictInfo is saved in database.
					// Insert merged_code, left_code, and right_code separately.
					insertCodeInChunks(mergeConflictInfo.getChunkIdDB(), mergeConflictInfo.getMergedCode(),
							"merged_code");
					insertCodeInChunks(mergeConflictInfo.getChunkIdDB(), mergeConflictInfo.getLeftCommitCode(),
							"left_code");
					insertCodeInChunks(mergeConflictInfo.getChunkIdDB(), mergeConflictInfo.getRightCommitCode(),
							"right_code");
				} else {
					throw new RuntimeException("MergeConflict Info was not saved to database!");
				}

			}
		}
	}

	/// if characterSize > 64000, then split the code, otherwise direct insert the
	/// code
	private void insertCodeInChunks(int chunkID, String code, String columnName)
			throws InvalidBeanException, SQLException {
		int characterSize = 64000;
		if (code != null) {
			if (code.length() > characterSize) {
				List<String> codeList = splitCodeInArray(code, characterSize);
				for (String currentCode : codeList) {
					mciDao.insertCode(chunkID, currentCode, columnName, true);
				}
			} else {
				mciDao.insertCode(chunkID, code, columnName, false);
			}
		}
	}

	/// Split the string into chunks of 64000 characters, insert it in array and
	/// return the array.
	private List<String> splitCodeInArray(String code, int characterSize) {
		List<String> codeArray = new ArrayList<String>();
		while (code.length() > 0) {
			if (code.length() < characterSize) {
				codeArray.add(code.substring(0, code.length()));
				code = "";
			} else {
				codeArray.add(code.substring(0, characterSize));
				code = code.substring(characterSize);
			}
		}
		return codeArray;
	}

	/**
	 * Persist Issues and related tables in the database
	 * 
	 * @param issue
	 * @throws SQLException
	 */
	public synchronized void persistIssueDB(Issue issue) throws SQLException {
		try {

			issue = persistIssue(issue);

			// Setting the labels of this "issue"
			for (String label : issue.getRelatedLabels()) {
				if (label != null && !label.equals("")) {
					LabelDao newLabel = persistLabel(new LabelDao(null, label));
					persistRelatedLabel(new RelatedLabelDao(null, issue.getIdDB(), newLabel));
				}
			}

			// Setting commits related to the issue
			for (Commit relatedCommit : issue.relatedCommits) {
				DeveloperNode newDev = new DeveloperNode(null, relatedCommit.getAuthor(),
						relatedCommit.getAuthorMail());
				DeveloperNode newCommitter = new DeveloperNode(null, relatedCommit.getCommitter(),
						relatedCommit.getCommitterMail());
				if (newDev.getName() == null && newDev.getEmail() == null) {
					continue;
				}
				if (newCommitter.getName() == null && newCommitter.getEmail() == null) {
					continue;
				}
				newDev = persistNode(newDev);
				newCommitter = persistNode(newCommitter);
				CommitN newCommit = new CommitN(null, issue.getProjectID(), newDev.getIdDB(), newCommitter.getIdDB(),
						relatedCommit.getId(), relatedCommit.getAuthorTime());
				newCommit = persistCommit(newCommit);
				persistIssueCommit(new IssueCommitDao(null, newCommit.getIdDB(), issue.getIdDB()));
			}

			// Setting the events related to this issue
			for (Event event : issue.getRelatedEvents()) {
				event.setIssueIdDB(issue.getIdDB());
				persistEvent(event);
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}

	}

	/**
	 * Persist networks and edges in the database
	 * 
	 * @param network
	 * @throws SQLException
	 */
	public synchronized void persistNetworkDB(Network network) throws SQLException {
		try {
			// Setting networks
			network = persistContNet(network);
			// Setting edges of this network
			for (DeveloperEdge edge : network.getDevEdges()) {
				edge.setNetworkIdDB(network.getIdDB());
				gettingLargerQueryForDeveloperEdge(edge);
				if (edgeRowList.length() > 65475) {
					savingEdgeRows();
				}
			}

			if (!edgeRowList.isEmpty()) {
				savingEdgeRows();
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Persist related Issues in the database
	 * 
	 * @param issueList
	 * @throws SQLException
	 */
	public synchronized void persistRelatedIssueDB(List<Issue> issueList) throws SQLException {
		try {
			for (Issue issue : issueList) {
				// Setting related issues to this "issue"
				for (Integer relatedIssue : issue.getRelatedIssues()) {
					Issue newRelatedIssue = new Issue(issue.getProjectID(), relatedIssue);
					newRelatedIssue = persistIssueFromRelatedIssue(newRelatedIssue);
					if (newRelatedIssue != null) {
						persistRelatedIssue(new RelatedIssueDao(null, issue.getIdDB(), newRelatedIssue.getIdDB()));
					}
				}
			}
		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete data from a contributor in many tables (merge scenarios, files,
	 * chunks, commit_chunks, commits, issue_commits, so on)
	 * 
	 * @param devIdDB
	 * @throws SQLException
	 */
	public synchronized void deleteContributorData(int devIdDB) throws SQLException {
		try {

			commitdao = new CommitDao();
			icdao = new IssueCommitDao();
			msdao = new MergeScenarioDao();
			fdao = new FileDao();
			cdao = new ChunkDao<>();
			ccdao = new ChunkCommitDao();

			ndao = new NetworkDao();
			nmdao = new NetworkMetricDao();
			msmdao = new MergeScenarioMetricDao();
			fmdao = new FileMetricDao();
			chmdao = new ChunkMetricDao();

			CommitN newCommitDao = new CommitN();

			Integer msId = null;
			List<Integer> fileIdList = new ArrayList<>();
			List<Integer> chunkIdList = new ArrayList<>();
			List<Integer> chunkCommitIdList = new ArrayList<>();

			Chunk<Object> newChunk = new Chunk<>();
			FileMS newFile = new FileMS();
			MergeScenario newMS = new MergeScenario();

			HashMap<Integer, String> aux = new HashMap<Integer, String>();
			aux.putAll(commitdao.getCommitsByContId(devIdDB));

			for (Entry<Integer, String> pair : aux.entrySet()) {
				if (icdao.isInIssueCommitTable(pair.getKey())) {
					icdao.delete(pair.getKey());
				}

				if (msdao.isThereMSByMergeCommitId(pair.getKey())) {
					msId = msdao.getMsIdByMergeCommitId(pair.getKey());
					fileIdList.addAll(fdao.getListOfFilesInMS(msId));
					for (Integer fileMsID : fileIdList) {
						chunkIdList.addAll(cdao.getListOfChunksInFileMS(fileMsID));
						for (Integer chunkID : chunkIdList) {
							chunkCommitIdList.addAll(ccdao.getListOfChunkCommitInChunk(chunkID));
						}
					}

					for (Integer chunkCommitId : chunkCommitIdList) {
						ccdao.setIdDB(chunkCommitId);
						ccdao.delete(ccdao);
					}

					for (Integer chunkId : chunkIdList) {
						newChunk.setIdDB(chunkId);
						ChunkMetrics chm = chmdao.get(new ChunkMetrics(newChunk.getIdDB()));
						if (chm != null) {
							chmdao.delete(chm);
						}
						cdao.delete(newChunk);
					}

					for (Integer fileId : fileIdList) {
						newFile.setIdDB(fileId);
						FileMetrics fm = fmdao.get(new FileMetrics(newFile.getIdDB()));
						if (fm != null) {
							fmdao.delete(fm);
						}
						fdao.delete(newFile);
					}

					newMS.setIdDB(msId);

					MergeScenarioMetrics msm = msmdao.get(new MergeScenarioMetrics(newMS.getIdDB()));
					if (msm != null) {
						msmdao.delete(msm);
					}

					Network net = ndao.get(new Network(null, newMS.getIdDB()));
					if (net != null) {
						NetworkMetrics netm = nmdao.get(new NetworkMetrics(net.getIdDB()));
						if (netm != null) {
							nmdao.delete(netm);
						}
						deleteEdgesFromNetwork(net, "edges");
						ndao.delete(net);
					}

					msdao.delete(newMS);

				}

				newCommitDao.setIdDB(pair.getKey());
				commitdao.delete(newCommitDao);
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Persist Project metrics in the database
	 * 
	 * @param projectMetrics
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public void persistProjectMetrics(ProjectMetrics projectMetrics) throws InvalidBeanException, SQLException {
		pmdao = new ProjectMetricsDao();
		ProjectMetrics aux = pmdao.get(projectMetrics);
		if (aux == null) {
			// Item not in database
			if (!pmdao.save(projectMetrics)) {
				throw new RuntimeException("Project Metrics was not saved to database!");
			}
		} else {
			projectMetrics.setIdDB(aux.getIdDB());
			if (!pmdao.update(projectMetrics)) {
				throw new RuntimeException("Project Metrics was not updated to database!");
			}
		}
	}

	/**
	 * Persist Network metrics in the database
	 * 
	 * @param netCoverageMetrics
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public synchronized void persistNetMetrics(List<NetworkMetrics> netCoverageMetrics)
			throws InvalidBeanException, SQLException {
		try {
			for (NetworkMetrics netMetric : netCoverageMetrics) {
				gettingLargerQueryForNetMetrics(netMetric);
				if (netMetricRowList.length() > 65400) {
					savingNetMetricsRows();
				}
			}

			if (!netMetricRowList.isEmpty()) {
				savingNetMetricsRows();
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private void gettingLargerQueryForNetMetrics(NetworkMetrics netMetric) throws InvalidBeanException, SQLException {

		if (netMetricRowList.isEmpty()) {
			netMetricRowList = ("insert into `net_metrics` ( `network_id`, `coverage_file_chunk`, `coverage_ms_file`, "
					+ "`coverage_chunk_comprehensive`, `coverage_chunk_precise`, `coverage_chunk_artifact`,`coverage_file_comprehensive`, "
					+ "`coverage_file_precise`, `coverage_file_artifact`, `coverage_ms_comprehensive`, `coverage_ms_precise`, "
					+ "`coverage_ms_artifact`, `coverage_comprehensive_chunk`, `coverage_precise_chunk`, `coverage_artifact_chunk`, "
					+ "`coverage_comprehensive_file`, `coverage_precise_file`, `coverage_artifact_file`, `coverage_comprehensive_ms`, "
					+ "`coverage_precise_ms`, `coverage_artifact_ms`, `number_comprehensive_edges`, `number_precise_edges`, "
					+ "`number_artifact_edges`, `number_chunk_edges`, `number_file_edges`, `number_ms_edges` ) values");
		}
		netMetricRowList = netMetricRowList + getNewNetMetricRow(netMetric);
	}

	private String getNewNetMetricRow(NetworkMetrics netMetric) {
		String space = ", ";
		String result = " (" + netMetric.getNetworkIdDB() + space + netMetric.getCovFileOverChunk() + space
				+ netMetric.getCovMSOverFile() + space + netMetric.getCovChunkBasedOverComprehensive() + space
				+ netMetric.getCovChunkBasedOverPrecise() + space + netMetric.getCovChunkBasedOverArtifact() + space
				+ netMetric.getCovFileBasedOverComprehensive() + space + netMetric.getCovFileBasedOverPrecise() + space
				+ netMetric.getCovFileBasedOverArtifact() + space + netMetric.getCovMSBasedOverComprehensive() + space
				+ netMetric.getCovMSBasedOverPrecise() + space + netMetric.getCovMSBasedOverArtifact() + space
				+ netMetric.getCovComprehensiveOverChunkBased() + space + netMetric.getCovPreciseOverChunkBased()
				+ space + netMetric.getCovArtifactOverChunkBased() + space
				+ netMetric.getCovComprehensiveOverFileBased() + space + netMetric.getCovPreciseOverFileBased() + space
				+ netMetric.getCovArtifactOverFileBased() + space + netMetric.getCovComprehensiveOverMSBased() + space
				+ netMetric.getCovPreciseOverMSBased() + space + netMetric.getCovArtifactOverMSBased() + space
				+ netMetric.getNumberComprehensiveEdges() + space + netMetric.getNumberPreciseEdges() + space
				+ netMetric.getNumberArtifactEdges() + space + netMetric.getNumberChunkEdges() + space
				+ netMetric.getNumberFileEdges() + space + netMetric.getNumberMSEdges() + "),";

		return result;
	}

	private void savingNetMetricsRows() throws InvalidBeanException, SQLException {
		nmdao = new NetworkMetricDao();
		netMetricRowList = netMetricRowList.substring(0, netMetricRowList.length() - 1);
		netMetricRowList = netMetricRowList + ";";

		if (!nmdao.saveManyRows(netMetricRowList)) {
			throw new RuntimeException("Network metric (net_metric) was not saved to database!");
		}
		netMetricRowList = "";
	}

	public void persistMsMetrics(List<MergeScenarioMetrics> msMetricsList) throws InvalidBeanException, SQLException {
		try {
			for (MergeScenarioMetrics msMetric : msMetricsList) {
				gettingLargerQueryForMSMetrics(msMetric);
				if (msMetricRowList.length() > 65400) {
					savingMSMetricsRows();
				}
			}

			if (!msMetricRowList.isEmpty()) {
				savingMSMetricsRows();
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private void gettingLargerQueryForMSMetrics(MergeScenarioMetrics msMetrics)
			throws InvalidBeanException, SQLException {

		if (msMetricRowList.isEmpty()) {
			msMetricRowList = ("insert into `ms_metrics` ( `merge_scenario_id`, `number_of_files`, `number_conflicted_files`, `number_left_files`, "
					+ " `number_right_files`,  `number_both_side_files`, `number_chunks`, `number_conflicted_chunks`, "
					+ " `number_left_chunks`, `number_right_chunks`, `number_commits`, `number_left_commits`, "
					+ "`number_right_commits`, `number_developers`, `number_left_dev`, `number_right_dev`, `number_both_side_dev`, `code_churn`, `conflict_code_churn`)"
					+ " values");
		}
		msMetricRowList = msMetricRowList + getNewMSMetricRow(msMetrics);
	}

	private String getNewMSMetricRow(MergeScenarioMetrics msMetrics) {
		String space = ", ";
		String result = " (" + msMetrics.getMsIdDB() + space + msMetrics.getNumberFiles() + space
				+ msMetrics.getNumberConflictedFiles() + space + msMetrics.getNumberLeftFiles() + space
				+ msMetrics.getNumberRightFiles() + space + msMetrics.getNumberBothSideFiles() + space
				+ msMetrics.getNumberChunks() + space + msMetrics.getNumberConflictedChunks() + space
				+ msMetrics.getNumberLeftChunks() + space + msMetrics.getNumberRightChunks() + space
				+ msMetrics.getNumberCommits() + space + msMetrics.getNumberLeftCommits() + space
				+ msMetrics.getNumberRightCommits() + space + msMetrics.getNumberDevelopers() + space
				+ msMetrics.getNumberLeftDevelopers() + space + msMetrics.getNumberRightDevelopers() + space
				+ msMetrics.getNumberBothSideDevelopers() + space + msMetrics.getCodeChurn() + space
				+ msMetrics.getConflictingCodeChurn() + "),";

		return result;
	}

	private void savingMSMetricsRows() throws InvalidBeanException, SQLException {
		msmdao = new MergeScenarioMetricDao();
		msMetricRowList = msMetricRowList.substring(0, msMetricRowList.length() - 1);
		msMetricRowList = msMetricRowList + ";";

		if (!msmdao.saveManyRows(msMetricRowList)) {
			throw new RuntimeException("MergeScenario metric (ms_metric) was not saved to database!");
		}
		msMetricRowList = "";
	}

	public void persistFileMetrics(List<FileMetrics> fileMetricsList) throws InvalidBeanException, SQLException {
		try {
			for (FileMetrics fileMetric : fileMetricsList) {
				gettingLargerQueryForFileMetrics(fileMetric);
				if (fileMetricRowList.length() > 65400) {
					savingFileMetricsRows();
				}
			}
			if (!fileMetricRowList.isEmpty()) {
				savingFileMetricsRows();
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private void gettingLargerQueryForFileMetrics(FileMetrics fileMetrics) throws InvalidBeanException, SQLException {

		if (fileMetricRowList.isEmpty()) {
			fileMetricRowList = ("insert into `file_metrics` ( `file_id`, `number_chunks`, `number_conflicted_chunks`, `number_left_chunks`, "
					+ "`number_right_chunks`, `number_commits`, `number_left_commits`, `number_right_commits`,"
					+ " `number_developers`, `number_left_dev`, `number_right_dev`, `number_both_side_dev`, `loc`)"
					+ " values");
		}
		fileMetricRowList = fileMetricRowList + getNewFileMetricRow(fileMetrics);
	}

	private String getNewFileMetricRow(FileMetrics fileMetrics) {
		String space = ", ";
		String result = " (" + fileMetrics.getFileIdDB() + space + fileMetrics.getNumberChunks() + space
				+ fileMetrics.getNumberConflictedChunks() + space + fileMetrics.getNumberLeftChunks() + space
				+ fileMetrics.getNumberRightChunks() + space + fileMetrics.getNumberCommits() + space
				+ fileMetrics.getNumberLeftCommits() + space + fileMetrics.getNumberRightCommits() + space
				+ fileMetrics.getNumberDevelopers() + space + fileMetrics.getNumberLeftDevelopers() + space
				+ fileMetrics.getNumberRightDevelopers() + space + fileMetrics.getNumberBothSideDevelopers() + space
				+ fileMetrics.getLoc() + "),";

		return result;
	}

	private void savingFileMetricsRows() throws InvalidBeanException, SQLException {
		fmdao = new FileMetricDao();
		fileMetricRowList = fileMetricRowList.substring(0, fileMetricRowList.length() - 1);
		fileMetricRowList = fileMetricRowList + ";";

		if (!fmdao.saveManyRows(fileMetricRowList)) {
			throw new RuntimeException("File metric (file_metric) was not saved to database!");
		}
		fileMetricRowList = "";
	}

	public void persistChunkMetrics(List<ChunkMetrics> chunkMetricsList) throws InvalidBeanException, SQLException {
		try {
			for (ChunkMetrics chunkMetric : chunkMetricsList) {
				gettingLargerQueryForChunkMetrics(chunkMetric);

				if (chunkMetricRowList.length() > 65400) {
					savingChunkMetricsRows();
				}
			}

			if (!chunkMetricRowList.isEmpty()) {
				savingChunkMetricsRows();
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private void gettingLargerQueryForChunkMetrics(ChunkMetrics chunkMetrics)
			throws InvalidBeanException, SQLException {

		if (chunkMetricRowList.isEmpty()) {
			chunkMetricRowList = ("insert into `chunk_metrics` ( `chunk_id`, `number_commits`, `number_left_commits`, "
					+ "`number_right_commits`, `number_developers`, `number_left_dev`, `number_right_dev`, "
					+ "`number_both_side_dev`) values");
		}
		chunkMetricRowList = chunkMetricRowList + getNewChunkMetricRow(chunkMetrics);
	}

	private String getNewChunkMetricRow(ChunkMetrics chunkMetrics) {
		String space = ", ";
		String result = " (" + chunkMetrics.getChunkIdDB() + space + chunkMetrics.getNumberCommits() + space
				+ chunkMetrics.getNumberLeftCommits() + space + chunkMetrics.getNumberRightCommits() + space
				+ chunkMetrics.getNumberDevelopers() + space + chunkMetrics.getNumberLeftDevelopers() + space
				+ chunkMetrics.getNumberRightDevelopers() + space + chunkMetrics.getNumberBothSideDevelopers() + "),";

		return result;
	}

	private void savingChunkMetricsRows() throws InvalidBeanException, SQLException {
		chmdao = new ChunkMetricDao();
		chunkMetricRowList = chunkMetricRowList.substring(0, chunkMetricRowList.length() - 1);
		chunkMetricRowList = chunkMetricRowList + ";";

		if (!chmdao.saveManyRows(chunkMetricRowList)) {
			throw new RuntimeException("Chunk metric (chunk_metric) was not saved to database!");
		}
		chunkMetricRowList = "";
	}

	public void deleteSpecificEdgeTypesByProject(Integer projectId, List<Integer> edgeTypeList)
			throws InvalidBeanException, SQLException {
		dedao = new DeveloperEdgeDao();
		List<Integer> edgesIdList = dedao.getEdgeIDListByProjectId(projectId, edgeTypeList);

		dedao.deleteManyRowsFromSpecificTable("edges", edgesIdList);
	}

	public List<CommitN> getCommitListFromDB(int projectId) throws SQLException {
		try {

			commitdao = new CommitDao();
			return commitdao.getCommitsByProject(projectId);

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	public synchronized void updateCommitDateInDB(List<CommitN> commitList) throws SQLException, InvalidBeanException {

		for (CommitN commit : commitList) {
			gettingLargerQueryForUpdatingCommits(commit);

			if (commitRowList.length() > 65200) {
				updatingCommitsDate();
			}
		}

		if (!commitRowList.isEmpty()) {
			updatingCommitsDate();
		}

	}

	private void gettingLargerQueryForUpdatingCommits(CommitN commit) throws InvalidBeanException, SQLException {

		if (commitRowList.isEmpty()) {
			commitRowList = ("insert into `commits` ( `id`, `project_id`, `contributor_id`, "
					+ "`committer_id`, `hash`, `commit_date`) values");
		}
		commitRowList = commitRowList + getCommitRow(commit);
	}

	private void updatingCommitsDate() throws InvalidBeanException, SQLException {
		commitdao = new CommitDao();

		commitRowList = commitRowList.substring(0, commitRowList.length() - 1);
		commitRowList = commitRowList + " on duplicate key update commit_date=values(commit_date);";

		if (!commitdao.updateManyRows(commitRowList)) {
			throw new RuntimeException("Commit was not updated to database!");
		}
		commitRowList = "";
	}

	private String getCommitRow(CommitN commit) {
		String space = ", ";
		@SuppressWarnings("deprecation")
		String result = " (" + commit.getIdDB() + space + commit.getProjectIdDB() + space + commit.getContIdDB() + space
				+ commit.getCommitterIdDB() + space + "\"" + commit.getHash() + "\"" + space + "\""
				+ commitdao.getFormattedDate(commit.getCommitDate().toGMTString()) + "\"),";

		return result;
	}

	public synchronized void deleteIssueData(Integer projectIdDB) throws SQLException {
		try {

			issuedao = new IssueDao();
			rldao = new RelatedLabelDao();
			ridao = new RelatedIssueDao();
			icdao = new IssueCommitDao();
			eventdao = new EventDao();

			List<Issue> issueList = issuedao.getIssuesFromDataBase(projectIdDB);

			for (Issue issue : issueList) {

				List<Integer> relatedLabelIdList = rldao.getByIssueId(issue.getIdDB());
				for (Integer rlId : relatedLabelIdList) {
					rldao.remove(rlId);
				}

				List<Integer> relatedIssuesIdList = ridao.getRelatedIssueIdListByIssue(issue.getIdDB());
				for (Integer rIssueId : relatedIssuesIdList) {
					ridao.remove(rIssueId);
				}

				List<Integer> issueCommitsList = icdao.getIssueCommitIdListByIssue(issue.getIdDB());
				for (Integer icId : issueCommitsList) {
					icdao.remove(icId);
				}

				for (Event event : issue.getRelatedEvents()) {
					eventdao.remove(event.getIdDB());
				}

				issuedao.remove(issue.getIdDB());

			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private void deleteEdgesFromNetwork(Network net, String tableName) throws InvalidBeanException, SQLException {
		dedao = new DeveloperEdgeDao();
		List<Integer> edgesIdList = dedao.getEdgeListByNetworkId(net.getIdDB());

		dedao.deleteManyRowsFromSpecificTable(tableName, edgesIdList);
	}

	public void deleteProjectMetricData(Integer projectIdDB) throws SQLException, InvalidBeanException {

		pmdao = new ProjectMetricsDao();

		ProjectMetrics projectMetric = pmdao.get(new ProjectMetrics(projectIdDB));

		pmdao.delete(projectMetric);

	}

	public void deleteNetworkMetricData(Integer projectIdDB, String tableName)
			throws SQLException, InvalidBeanException {

		nmdao = new NetworkMetricDao();

		List<Integer> netMetricIdList = nmdao.getNetMetricsIdListByProject(projectIdDB);

		nmdao.deleteManyRowsFromSpecificTable(tableName, netMetricIdList);

	}

	public void deleteMSMetricData(Integer projectIdDB, String tableName) throws SQLException, InvalidBeanException {

		msmdao = new MergeScenarioMetricDao();
		List<Integer> msMetricIdList = msmdao.getMsMetricsListIdByProject(projectIdDB);

		msmdao.deleteManyRowsFromSpecificTable(tableName, msMetricIdList);
	}

	public void deleteFileMetricData(Integer projectIdDB, String tableName) throws SQLException, InvalidBeanException {

		fmdao = new FileMetricDao();
		List<Integer> fileMetricIdList = fmdao.getFileMetricsIdListByProject(projectIdDB);

		fmdao.deleteManyRowsFromSpecificTable(tableName, fileMetricIdList);

	}

	public void deleteChunkMetricData(Integer projectIdDB, String tableName) throws SQLException, InvalidBeanException {

		chmdao = new ChunkMetricDao();
		List<Integer> chunkMetricList = chmdao.getChunkMetricsIdListByProject(projectIdDB);

		chmdao.deleteManyRowsFromSpecificTable(tableName, chunkMetricList);

	}

	public synchronized void persistDeveloperRole(DeveloperRole devRole, String tableName)
			throws InvalidBeanException, SQLException {

		devRoleDao = new DeveloperRoleDao();
		DeveloperRole aux = devRoleDao.getDeveloper(devRole, tableName);
		if (aux == null) {
			if (!devRoleDao.save(devRole, tableName)) {
				throw new RuntimeException("Committer was not saved to database!");
			}
		}
	}

	public synchronized void persistDeveloperList(List<Developer> devList) throws InvalidBeanException, SQLException {

		try {
			for (Developer dev : devList) {
				gettingLargerQueryForDevs(dev);
				if (devRowList.length() > 65200) {
					savingDevRows();
				}
			}

			if (!devRowList.isEmpty()) {
				savingDevRows();
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private synchronized void gettingLargerQueryForDevs(Developer dev) throws InvalidBeanException, SQLException {
		if (devRowList.isEmpty()) {
			devRowList = ("insert into `devs` (`merge_scenario_id`, `contributor_id`, `number_files_left`, `number_files_right`, "
					+ "`number_chunks_left`, `number_chunks_right`,`number_changed_lines_left`, `number_changed_lines_right`,"
					+ " `number_commits_left`, `number_commits_right`, `last_change_left`, `last_change_right`, `is_integrator`,"
					+ " `contribute_conflict`, `is_core_dev`, `is_left_branch_leader`, `is_right_branch_leader`) values");
		}
		devRowList = devRowList + getNewDevRow(dev);
	}

	private String getNewDevRow(Developer dev) {
		String space = ", ";
		String result = " (" + dev.getMergeScenarioIdDB() + space + dev.getContributorIdDB() + space
				+ dev.getNumberOfFilesChangedLeft() + space + dev.getNumberOfFilesChangedRight() + space
				+ dev.getNumberOfChunksChangedLeft() + space + dev.getNumberOfChunksChangedRight() + space
				+ dev.getNumberOfLinesChangedLeft() + space + dev.getNumberOfLinesChangedRight() + space
				+ dev.getNumberOfCommitsLeft() + space + dev.getNumberOfCommitsRight() + space + dev.isLastChangeLeft()
				+ space + dev.isLastChangeRight() + space + dev.isIntegrator() + space + dev.isContributeToConflict()
				+ space + dev.isCoreDeveloper() + space + dev.isLeftBranchLeader() + space + dev.isRightBranchLeader()
				+ "),";

		return result;

	}

	private synchronized void savingDevRows() throws InvalidBeanException, SQLException {
		devRoleDao = new DeveloperRoleDao();
		devRowList = devRowList.substring(0, devRowList.length() - 1);
		devRowList = devRowList + ";";

		if (!devRoleDao.saveManyRows(devRowList)) {
			throw new RuntimeException("Devs could not be stored to database!");
		}
		devRowList = "";
	}

	public synchronized void updateCommitFieldsInDB(List<CommitN> commitList)
			throws InvalidBeanException, SQLException {
		commitdao = new CommitDao();
		for (CommitN commit : commitList) {
			if (!commitdao.updateCommitterIdDB(commit)) {
				throw new RuntimeException("Commit" + commit.getIdDB() + " was not updated to database!");
			}
		}
	}

	public synchronized void setCommitterIntoDB(Integer msIdDB, List<Integer> committerIdDBList)
			throws InvalidBeanException, SQLException {
		devRoleDao = new DeveloperRoleDao();
		for (Integer committerIdDB : committerIdDBList) {
			DeveloperRole newCommitter = new DeveloperRole(null, msIdDB, committerIdDB);
			DeveloperRole aux = devRoleDao.getDeveloper(newCommitter, "committers");
			if (aux == null) {
				// Item not in database
				if (!devRoleDao.save(newCommitter, "committers")) {
					throw new RuntimeException("Committer was not saved to database!");
				}
			}
		}

	}

	public synchronized void persistDeveloperRoleList(List<DeveloperRole> devRoleList, String tableName)
			throws InvalidBeanException, SQLException {
		devRoleDao = new DeveloperRoleDao();

		for (DeveloperRole devRole : devRoleList) {
			if (!devRoleDao.save(devRole, tableName)) {
				throw new RuntimeException("The List of DeveloperRole was not saved to database!");
			}
		}
	}

	/**
	 * Persist merge_conflict metrics in the database
	 * 
	 * @param mergeConflictMetricsList
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	public synchronized void persistMergeConflictMetrics(List<MergeConflictMetrics> mergeConflictMetricsList,
			boolean isNew) throws InvalidBeanException, SQLException {
		try {
			for (MergeConflictMetrics mcm : mergeConflictMetricsList) {
				gettingLargerQueryForMergeConflictMetrics(mcm, isNew);
				if (MergeConflictMetricRowString.length() > 65350) {
					savingMergeConflictMetricsRows(isNew);
				}
			}

			if (!MergeConflictMetricRowString.isEmpty()) {
				savingMergeConflictMetricsRows(isNew);
			}

		} catch (InvalidBeanException e) {
			Logger.logStackTrace(log, e);
			throw new RuntimeException(e);
		}
	}

	private void gettingLargerQueryForMergeConflictMetrics(MergeConflictMetrics mergeConflictMetric, boolean isNew)
			throws InvalidBeanException, SQLException {

		if (MergeConflictMetricRowString.isEmpty()) {
			MergeConflictMetricRowString = ("insert into `merge_conflict_metrics` (");
			if (!isNew) {
				MergeConflictMetricRowString = MergeConflictMetricRowString + "`id`, ";
			}
			MergeConflictMetricRowString = MergeConflictMetricRowString
					+ ("`merge_conflict_info_id`, `change_type`, `loc`, `left_loc`, `right_loc`, `cyclomatic_complexity`,"
							+ "`left_cyclomatic_complexity`, `right_cyclomatic_complexity`, `dev_has_knowledge` ) values");
		}
		MergeConflictMetricRowString = MergeConflictMetricRowString + getNewNetMetricRow(mergeConflictMetric, isNew);
	}

	private String getNewNetMetricRow(MergeConflictMetrics mergeConflictMetric, boolean isNew) {
		String space = ", ";
		String result = " (";
		if (!isNew) {
			result = result + mergeConflictMetric.getIdDB() + space;
		}
		result = result + mergeConflictMetric.getMergeConflictInfoIdDB() + space + "\""
				+ mergeConflictMetric.getChangeType() + "\"" + space + mergeConflictMetric.getLoc() + space
				+ mergeConflictMetric.getLeftLoc() + space + mergeConflictMetric.getRightLoc() + space
				+ mergeConflictMetric.getCyclomaticComplexity() + space
				+ mergeConflictMetric.getLeftCyclomaticComplexity() + space
				+ mergeConflictMetric.getRightCyclomaticComplexity() + space + mergeConflictMetric.isDevHasKnowledge()
				+ "),";

		return result;
	}

	private void savingMergeConflictMetricsRows(boolean isNew) throws InvalidBeanException, SQLException {
		mcmdao = new MergeConflictMetricsDao();
		MergeConflictMetricRowString = MergeConflictMetricRowString.substring(0,
				MergeConflictMetricRowString.length() - 1);
		if (!isNew) {
			MergeConflictMetricRowString = MergeConflictMetricRowString
					+ "ON DUPLICATE KEY UPDATE change_type=VALUES(change_type), dev_has_knowledge=VALUES(dev_has_knowledge)";
		} else {
			MergeConflictMetricRowString = MergeConflictMetricRowString + ";";
		}

		if (!mcmdao.saveManyRows(MergeConflictMetricRowString)) {
			throw new RuntimeException("merge_conflict metric (merge_conflict_metric) was not saved to database!");
		}
		MergeConflictMetricRowString = "";
	}

	public synchronized void persistCommitAndRelatedData(Integer projectIdDB, Commit commit)
			throws SQLException, InvalidBeanException {

		DeveloperNode newDev = new DeveloperNode(null, commit.getAuthor(), commit.getAuthorMail());
		newDev = persistNode(newDev);
		DeveloperNode newCommitter = new DeveloperNode(null, commit.getCommitter(), commit.getCommitterMail());
		newCommitter = persistNode(newCommitter);
		CommitN newCommit = new CommitN(null, projectIdDB, newDev.getIdDB(), newCommitter.getIdDB(), commit.getId(),
				commit.getAuthorTime());
		newCommit = persistCommit(newCommit);

	}
}
