package commnet.builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.activation.MimetypesFileTypeMap;

import gitwrapper.process.ProcessExecutor.ExecRes;
import gitwrapper.repo.BlameLine;
import gitwrapper.repo.Commit;
import gitwrapper.repo.GitWrapper;
import gitwrapper.repo.MergeConflict;
import gitwrapper.repo.Status;
import commnet.model.beans.Chunk;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.FileMS;
import commnet.model.beans.MergeScenario;
import commnet.model.beans.Project;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.IOHandler;
import commnet.util.Logger;

public class MergeScenarioBuilder implements Runnable {

	private File log;
	private File unusedMergeCommitsFile;
	private Integer chunkID = 1;
	private List<MergeScenario> mergeScenariosList = new ArrayList<MergeScenario>();

	private Project project;
	private GitWrapper git;
	private List<FileMS> deletedFilesList = new ArrayList<>();
	private boolean simpleDatabaseSearch;
	private List<String> mergeCommitsList;

	public MergeScenarioBuilder() {

	}

	public MergeScenarioBuilder(Project project, boolean simpleDatabaseSearch, List<String> mergeCommitsList) {
		setProject(project);
		git = getProject().getRepository().getGit();
		setSimpleDatabaseSearch(simpleDatabaseSearch);
		setMergeCommitsList(mergeCommitsList);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setLogFile(File log) {
		this.log = log;
	}

	public void setUnusedMCFile(File f) {
		this.unusedMergeCommitsFile = f;
	}

	public Integer getChunkID() {
		return chunkID;
	}

	public void incrementChunkID() {
		this.chunkID++;
	}

	public List<MergeScenario> getMergeScenariosList() {
		return mergeScenariosList;
	}

	public void setMergeScenariosList(List<MergeScenario> mergeScenariosList) {
		this.mergeScenariosList = mergeScenariosList;
	}

	public boolean isSimpleDatabaseSearch() {
		return simpleDatabaseSearch;
	}

	public void setSimpleDatabaseSearch(boolean simpleDatabaseSearch) {
		this.simpleDatabaseSearch = simpleDatabaseSearch;
	}

	public List<String> getMergeCommitsList() {
		return mergeCommitsList;
	}

	public void setMergeCommitsList(List<String> mergeCommitsList) {
		this.mergeCommitsList = mergeCommitsList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			if (!isSimpleDatabaseSearch()) {
				setMergeScenariosList(getMergeScenarios());
			} else if (!mergeCommitsList.isEmpty()) {
				setMergeScenariosList(buildSpecificMergeScenariosFromDatabase());
			} else {
				setMergeScenariosList(buildMergeScenariosFromDatabase());
			}
		} catch (IOException e) {
			Logger.logStackTrace(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Build merge scenarios of a project
	 * 
	 * @return list of merge scenarios
	 * @throws IOException
	 */
	public List<MergeScenario> getMergeScenarios() throws IOException {

		Logger.log(log, "MergeScenario building start.");
		git.exec(getProject().getRepository().getDir(), "reset", "--hard");
		Optional<List<Commit>> mergeCommitsOpitionalList = getProject().getRepository().getMergeCommits();
		List<MergeScenario> result = new ArrayList<MergeScenario>();

		Optional<ExecRes> aux = git.exec(getProject().getRepository().getDir(), "rev-list", "--all", "--max-count=1");

		List<String> mergeCommitList = new ArrayList<>();
		List<MergeScenario> auxMergeList = new ArrayList<>();
		List<Commit> commitList = new ArrayList<>();
		commitList.addAll(mergeCommitsOpitionalList.get());

		try {
			MergeScenarioDao mergeDao = new MergeScenarioDao();

			String chunkHasConflict = "";
			auxMergeList = mergeDao.getMergeScenariosFromDataBase(project.getIdDB(), chunkHasConflict);
			result.addAll(auxMergeList);

			mergeCommitList = mergeDao.getMergeCommitsOfProject(project.getIdDB());
		} catch (SQLException | InvalidBeanException e1) {
			e1.printStackTrace();
			Logger.log(log, "ERROR GETTING MERGE COMMITS in DATABASE");
		}

		// OffsetDateTime startingDate =
		// OffsetDateTime.parse("2011-01-01T00:00:00+01:00");

		IOHandler iohandler = new IOHandler();
		List<String> unusedMergeCommitList = new ArrayList<>();
		if (unusedMergeCommitsFile.exists() && !unusedMergeCommitsFile.isDirectory()) {
			unusedMergeCommitList = iohandler.readFile(unusedMergeCommitsFile);
		}
		for (Commit auxCommit : mergeCommitsOpitionalList.get()) {
			if (mergeCommitList.contains(auxCommit.getId().toString())
					// || auxCommit.getAuthorTime().isBefore(startingDate)
					|| unusedMergeCommitList.contains(auxCommit.getId().toString())) {
				commitList.remove(auxCommit);
			}
		}

		ListIterator<Commit> listIter = commitList.listIterator(commitList.size());

		while (listIter.hasPrevious()) {

			Commit mergeCommit = listIter.previous();

			if (mergeCommit == null || mergeCommit.getAuthorTime() == null) {
				if (listIter.hasPrevious()) {
					mergeCommit = listIter.previous();
				}
			}

			MergeScenario newMergeScenario = new MergeScenario(project.getIdDB(), mergeCommit);

			Optional<Status> isConflicted = newMergeScenario.getLeftParentCommit()
					.merge(newMergeScenario.getRightParentCommit());

			if (!isConflicted.isPresent() || newMergeScenario.getBaseCommit() == null) {
				Logger.log(unusedMergeCommitsFile, mergeCommit.getId().toString());
				continue;
			}

			deletedFilesList.clear();
			// it is made for all files of a merge scenario
			for (Path file : getAffectedPaths(newMergeScenario, false)) {

				FileMS newFile = new FileMS(file);
				// getting just conflicted merge scenarios
				if (isConflicted.get().hasConflicts()) {
					Optional<List<MergeConflict>> blameLineListConflictedMerge = getProject().getRepository()
							.blameUnmergedFile(file);

					if (!blameLineListConflictedMerge.isPresent()) {
						deletedFilesList.add(newFile);
						continue;

					}

					for (MergeConflict merge : blameLineListConflictedMerge.get()) {
						List<BlameLine> listLeft = merge.left;
						List<BlameLine> listRight = merge.right;
						List<Commit> leftCommitList = new ArrayList<Commit>();
						List<Commit> rightCommitList = new ArrayList<Commit>();

						Chunk<Object> newChunk = new Chunk<Object>();
						newChunk.setID(getChunkID());
						Integer countLines = 0;
						if (listLeft.isEmpty()) {
							newChunk.setBeginLine(1);
						} else {
							newChunk.setBeginLine(listLeft.get(0).finalLineNumber - 1);
							countLines = listLeft.get(0).finalLineNumber - 1;
						}

						// get commits between the base and the merge commit
						// LEFT SIDE
						for (BlameLine line : listLeft) {
							if (!leftCommitList.contains(line.commit)
									&& (line.commit.checkAncestry(newMergeScenario.getBaseCommit()).orElse(false))
									&& (!line.commit.equals(newMergeScenario.getBaseCommit()))) {
								leftCommitList.add(line.commit);
							}
							countLines++;
						}
						countLines++;
						newChunk.setLeftCommitList(leftCommitList);

						// RIGHT SIDE
						for (BlameLine line : listRight) {
							if (!rightCommitList.contains(line.commit)
									&& (line.commit.checkAncestry(newMergeScenario.getBaseCommit()).orElse(false))
									&& (!line.commit.equals(newMergeScenario.getBaseCommit()))) {
								rightCommitList.add(line.commit);
							}
							countLines++;
						}
						countLines++;
						newChunk.setRightCommitList(rightCommitList);
						newChunk.setEndLine(countLines);

						// Double check. In a specific case it can be considered
						// a chunk conflict, but it can actually not be.
						if (!leftCommitList.isEmpty() && !rightCommitList.isEmpty()) {
							newMergeScenario.setHasConflict(true);
							newChunk.isConflict(true);
							newFile.setConflict(true);
						}

						// GETTING NODES FOR BOTH SIDES
						if (!leftCommitList.isEmpty()) {
							newChunk.setLeftDevList(getDeveloperNodes(leftCommitList));
						}
						if (!rightCommitList.isEmpty()) {
							newChunk.setRightDevList(getDeveloperNodes(rightCommitList));
						}

						for (Commit auxcommit : leftCommitList) {
							if (!newMergeScenario.getListCommitMergeScenario().contains(auxcommit)) {
								newMergeScenario.getListCommitMergeScenario().add(auxcommit);
							}
						}

						for (Commit auxcommit : rightCommitList) {
							if (!newMergeScenario.getListCommitMergeScenario().contains(auxcommit)) {
								newMergeScenario.getListCommitMergeScenario().add(auxcommit);
							}
						}

						newFile.getLeftFileDeveloperMap().put(newChunk.getID(), newChunk.getLeftDevList());
						newFile.getRightFileDeveloperMap().put(newChunk.getID(), newChunk.getRightDevList());

						newFile.getChunkList().add(newChunk);
						incrementChunkID();

					}

					// GETING CHUNKS FROM NON-CONFLICTED FILES IN CONFLICTED
					// MERGES
					getNonConflictedChunks(newMergeScenario, newFile);

				} else {

					// GETING NON-CONFLICT CHUNKS IN NON CONFLICT FILES
					getNonConflictedChunks(newMergeScenario, newFile);
				}

				if (!newFile.getLeftFileDeveloperMap().isEmpty()) {
					newMergeScenario.getLeftMergeDeveloperMap().put(file.toString(),
							newFile.getLeftFileDeveloperList());
				}
				if (!newFile.getRightFileDeveloperMap().isEmpty()) {
					newMergeScenario.getRightMergeDeveloperMap().put(file.toString(),
							newFile.getRightFileDeveloperList());
				}

				// Add only files with some chunk
				if (!newFile.getChunkList().isEmpty()) {
					newMergeScenario.getListFileMS().add(newFile);
				}
			}

			if (!deletedFilesList.isEmpty()) {
				buildingChunksToDeletedFiles(newMergeScenario);

			}

			if (newMergeScenario.getListFileMS().isEmpty()) {
				Logger.log(unusedMergeCommitsFile, mergeCommit.getId().toString());
				continue;
			}

			git.exec(getProject().getRepository().getDir(), "reset", "--hard");

			try {
				DBWriter.INSTANCE.persistMergeScenarioDB(project.getIdDB(), newMergeScenario);
			} catch (SQLException e) {
				e.printStackTrace();
				Logger.log(log, "ERROR PERSISTING MERGE in DATABASE");
			}

			System.out.println("MERGE : " + newMergeScenario.getIdDB() + " mergeCommit : " + mergeCommit.getId()
					+ " has conflict? " + newMergeScenario.getHasConflict());

			System.out.println("==============");

			result.add(newMergeScenario);
		}
		git.exec(getProject().getRepository().getDir(), "checkout", aux.get().stdOut);
		Logger.log(log, "MergeScenario building finished.");
		return result;

	}

	/**
	 * This methods get files which blame failed and create a chunk and file to such
	 * files case the changes happened in a merge scenario. Normally, deleted files
	 * will fail
	 * 
	 * @param newMergeScenario
	 */
	private void buildingChunksToDeletedFiles(MergeScenario newMergeScenario) {

		for (FileMS file : deletedFilesList) {
			if (!newMergeScenario.getListFileMS().contains(file)) {

				Chunk<Object> newAuxChunk = new Chunk<>(null, null, 1, 1, false);
				newAuxChunk.setID(getChunkID());

				// Reseting changes case there is conflict
				git.exec(getProject().getRepository().getDir(), "reset", "--hard");

				// Going to the merge commit
				git.exec(getProject().getRepository().getDir(), "checkout", newMergeScenario.getMergeCommit().getId());

				Optional<ExecRes> commithashExec = git.exec(getProject().getRepository().getDir(), "log", "-1",
						"--format=%H", "--", file.getFileName());
				String commithash = commithashExec.get().stdOut;
				commithash = commithash.replace("\n", "");
				Optional<Commit> commitOptional = getProject().getRepository().getCommit(commithash);

				List<Commit> commitList = new ArrayList<>();
				if (!commitOptional.isPresent()) {
					continue;
				}
				commitList.add(commitOptional.get());

				List<DeveloperNode> newDev = getDeveloperNodes(commitList);

				if (commitList.get(0).checkAncestry(newMergeScenario.getBaseCommit()).orElse(false)) {
					if (newMergeScenario.getLeftParentCommit().checkAncestry(commitList.get(0)).orElse(false)) {
						newAuxChunk.getLeftCommitList().add(commitList.get(0));
						newAuxChunk.getLeftDevList().add(newDev.get(0));
						file.getLeftFileDeveloperMap().put(getChunkID(), newDev);
						incrementChunkID();
					} else if (newMergeScenario.getRightParentCommit().checkAncestry(commitList.get(0)).orElse(false)) {
						newAuxChunk.getRightCommitList().add(commitList.get(0));
						newAuxChunk.getRightDevList().add(newDev.get(0));
						file.getRightFileDeveloperMap().put(getChunkID(), newDev);
						incrementChunkID();
					} else {
						continue;
					}
				} else {
					continue;
				}

				git.exec(getProject().getRepository().getDir(), "checkout", newMergeScenario.getBaseCommit().getId());

				Optional<List<BlameLine>> blameLineList = getProject().getRepository().blameFile(file.getPath());
				if (!blameLineList.isPresent()) {
					continue;
				}

				Integer numberOfLines = blameLineList.get().size();
				newAuxChunk.setEndLine(numberOfLines);
				file.getChunkList().add(newAuxChunk);

				if (!newMergeScenario.getListCommitMergeScenario().contains(commitList.get(0))) {
					newMergeScenario.getListCommitMergeScenario().add(commitList.get(0));
				}

				newMergeScenario.getListFileMS().add(file);
			}
		}
	}

	/**
	 * Get affected paths (files) in a merge scenario excluding binary and empty
	 * files
	 * 
	 * @param isConflicted
	 * @return list of paths
	 * @throws IOException
	 */
	List<Path> getAffectedPaths(MergeScenario mergeScenario, boolean onlyConflictingPaths) throws IOException {
		List<Path> affectedPaths = new ArrayList<Path>();
		Optional<ExecRes> aux;

		if (!onlyConflictingPaths) {
			aux = git.exec(getProject().getRepository().getDir(), "diff", "--stat", "--name-only",
					mergeScenario.getBaseCommit().getId(), mergeScenario.getMergeCommit().getId());
		} else {
			aux = git.exec(getProject().getRepository().getDir(), "diff", "--stat", "--name-only");
		}

		List<Path> newPathList = new ArrayList<>();

		String string = aux.get().stdOut;
		while (!string.isEmpty()) {
			int index = string.indexOf("\n");
			String newString = string.substring(0, index);
			string = string.substring(index + 1);
			newPathList.add(Paths.get(newString));
		}

		for (Path file : newPathList) {
			try {
				if (!isBinary(file) && !affectedPaths.contains(file)) {
					affectedPaths.add(file);
				}
			} catch (IOException e) {
				Logger.log(log, "It broke cheking if a file is binary.");
				e.printStackTrace();
			}
		}
		return affectedPaths;
	}

	/**
	 * Check if a Path is binary
	 * 
	 * @param f
	 * @return false if not otherwise true
	 * @throws IOException
	 */
	public boolean isBinary(Path f) throws IOException {

		final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
		String type = Files.probeContentType(f);
		if (type == null) {
			type = fileTypeMap.getContentType(f.toString());
		}

		if (type == null || type.startsWith("application/vnd") || type.startsWith("application/x-java")
				|| type.startsWith("application/font") || type.startsWith("application/x-trash")
				|| type.startsWith("application/java-vm") || type.startsWith("application/octet-stream")
				|| type.startsWith("application/java-archive")) {
			return true;
		}
		if (type.startsWith("text") || type.startsWith("application")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get chunks that are not part of a conflict
	 * 
	 * @param mergeScenario
	 * @param newFile
	 */
	private void getNonConflictedChunks(MergeScenario mergeScenario, FileMS newFile) {

		Optional<List<BlameLine>> blameLineList = getProject().getRepository().blameFile(newFile.getPath());

		Map<Integer, BlameLine> listNonConflictedLines = new TreeMap<Integer, BlameLine>();
		List<Chunk<Object>> chunkList = new ArrayList<Chunk<Object>>();

		if (!blameLineList.isPresent()) {
			deletedFilesList.add(newFile);
		}

		if (mergeScenario.getBaseCommit() != null) {
			// checking if getting the blame information did not fail
			// if fail return the empty list (chunkList)
			if (blameLineList.isPresent() && !blameLineList.get().isEmpty()) {
				// getting all lines between the commit base and merge commit
				for (BlameLine line : blameLineList.get()) {
					if (!line.commit.getId().equals("0000000000000000000000000000000000000000")
							&& !line.commit.equals(mergeScenario.getBaseCommit())
							&& line.commit.checkAncestry(mergeScenario.getBaseCommit()).orElse(false)) {
						listNonConflictedLines.put(line.finalLineNumber, line);
					}
				}
			}
		}

		// removing lines that are in conflict chunks
		for (Chunk<?> chunk : newFile.getChunkList()) {
			Integer beginLine = chunk.getBeginLine();
			Integer endLine = chunk.getEndLine();
			while (beginLine <= endLine) {
				listNonConflictedLines.remove(beginLine);
				beginLine++;
			}
		}

		// Creating chunks with the remaining lines
		Integer contIteractions = 0;
		for (Integer lineI : listNonConflictedLines.keySet()) {
			if (lineI > contIteractions) {
				Chunk<Object> newChunk = new Chunk<Object>();
				newChunk.setBeginLine(lineI);
				newChunk.setID(getChunkID());
				newChunk.isConflict(false);

				List<Commit> leftCommitList = new ArrayList<>();
				List<Commit> rightCommitList = new ArrayList<>();

				// if the left parent is ancestral of the commit from lineI or
				// vice versa it sets true
				if (listNonConflictedLines.get(lineI).commit.checkAncestry(mergeScenario.getLeftParentCommit())
						.orElse(false)
						|| mergeScenario.getLeftParentCommit().checkAncestry(listNonConflictedLines.get(lineI).commit)
								.orElse(false)) {
					leftCommitList.add(listNonConflictedLines.get(lineI).commit);
				} else {
					rightCommitList.add(listNonConflictedLines.get(lineI).commit);
				}

				for (Integer lineJ : listNonConflictedLines.keySet()) {
					if (lineJ > lineI) {
						if (lineJ.equals(lineI + 1)) {
							lineI++;
							contIteractions = lineI;
							if (listNonConflictedLines.get(lineJ).commit
									.checkAncestry(mergeScenario.getLeftParentCommit()).orElse(false)
									|| mergeScenario.getLeftParentCommit()
											.checkAncestry(listNonConflictedLines.get(lineJ).commit).orElse(false)) {
								if (!leftCommitList.contains(listNonConflictedLines.get(lineI).commit)) {
									leftCommitList.add(listNonConflictedLines.get(lineI).commit);
								}
							} else {
								if (!rightCommitList.contains(listNonConflictedLines.get(lineI).commit)) {
									rightCommitList.add(listNonConflictedLines.get(lineI).commit);
								}
							}
						} else {
							break;
						}
					}
				}
				newChunk.setLeftCommitList(leftCommitList);
				newChunk.setRightCommitList(rightCommitList);
				newChunk.setEndLine(lineI);
				chunkList.add(newChunk);
				incrementChunkID();

			}
		}

		// Adding chunks in the file and
		// Adding edges and developers that contribute to each chunk
		if (!chunkList.isEmpty()) {
			for (Chunk<Object> auxChunk : chunkList) {
				newFile.getChunkList().add(auxChunk);

				// GETTING EDGES FOR LEFT SIDE - Type 1
				if (!auxChunk.getLeftCommitList().isEmpty()) {
					auxChunk.setLeftDevList(getDeveloperNodes(auxChunk.getLeftCommitList()));

					newFile.getLeftFileDeveloperMap().put(auxChunk.getID(), auxChunk.getLeftDevList());
					for (Commit auxcommit : auxChunk.getLeftCommitList()) {
						if (!mergeScenario.getListCommitMergeScenario().contains(auxcommit)) {
							mergeScenario.getListCommitMergeScenario().add(auxcommit);
						}
					}

				}
				// GETTING EDGES FOR RIGHT SIDE - Type 1
				if (!auxChunk.getRightCommitList().isEmpty()) {
					auxChunk.setRightDevList(getDeveloperNodes(auxChunk.getRightCommitList()));

					newFile.getRightFileDeveloperMap().put(auxChunk.getID(), auxChunk.getRightDevList());
					for (Commit auxcommit : auxChunk.getRightCommitList()) {
						if (!mergeScenario.getListCommitMergeScenario().contains(auxcommit)) {
							mergeScenario.getListCommitMergeScenario().add(auxcommit);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the developers that contribute to a chunk based on commits that change
	 * something after the base commit until the merge commit
	 * 
	 * @param commitList
	 * @return list of DeveloperNodes
	 */
	private List<DeveloperNode> getDeveloperNodes(List<Commit> commitList) {

		List<DeveloperNode> developerList = new ArrayList<DeveloperNode>();

		for (Commit commit : commitList) {
			DeveloperNode newDev = new DeveloperNode();
			newDev.setName(commit.getAuthor());
			newDev.setEmail(commit.getAuthorMail());

			// Avoid same developers that contribute in different commits
			if (!developerList.contains(newDev)) {
				developerList.add(newDev);
			}

		}
		return developerList;
	}

	private List<MergeScenario> buildSpecificMergeScenariosFromDatabase() throws IOException {

		List<MergeScenario> result = new ArrayList<MergeScenario>();
		try {
			MergeScenarioDao mergeDao = new MergeScenarioDao();
			for (String mergeCommit : mergeCommitsList) {
				result.add(mergeDao.getMergeScenarioByHashFromDataBase(mergeCommit));
			}
		} catch (SQLException | InvalidBeanException e1) {
			e1.printStackTrace();
			Logger.log(log, "ERROR GETTING MERGE COMMITS in DATABASE");
		}

		return result;
	}

	public List<MergeScenario> buildMergeScenariosFromDatabase() throws IOException {

		List<MergeScenario> result = new ArrayList<MergeScenario>();
		try {
			MergeScenarioDao mergeDao = new MergeScenarioDao();
			String chunkHasConflict = "";
			result = mergeDao.getMergeScenariosFromDataBase(project.getIdDB(), chunkHasConflict);
		} catch (SQLException | InvalidBeanException e1) {
			e1.printStackTrace();
			Logger.log(log, "ERROR GETTING MERGE COMMITS in DATABASE");
		}

		return result;
	}

}
