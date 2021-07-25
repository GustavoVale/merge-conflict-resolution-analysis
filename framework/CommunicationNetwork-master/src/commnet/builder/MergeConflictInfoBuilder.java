package commnet.builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gitwrapper.repo.BlameLine;
import gitwrapper.repo.Commit;
import gitwrapper.repo.GitWrapper;
import gitwrapper.repo.MergeConflict;
import gitwrapper.repo.Status;
import commnet.model.beans.Chunk;
import commnet.model.beans.FileMS;
import commnet.model.beans.MergeConflictInfo;
import commnet.model.beans.MergeScenario;
import commnet.model.beans.Project;
import commnet.model.dao.CommitDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class MergeConflictInfoBuilder {

	private List<MergeConflictInfo> mergeConflictInfoList = new ArrayList<MergeConflictInfo>();

	private Project project;
	private GitWrapper git;
	private File log;

	private CommitDao commitDao = new CommitDao();
	private MergeScenarioDao mergeDao = new MergeScenarioDao();
	private MergeScenarioBuilder mergeBuilder;

	public MergeConflictInfoBuilder(Project project, File log) {
		setProject(project);
		git = getProject().getRepository().getGit();
		setLogFile(log);
		setMergeConflictInfoList(new ArrayList<MergeConflictInfo>());
		mergeBuilder = new MergeScenarioBuilder(project, false, null);

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

	public List<MergeConflictInfo> getMergeConflictInfoList() {
		return mergeConflictInfoList;
	}

	public void setMergeConflictInfoList(List<MergeConflictInfo> mergeConflictInfoList) {
		this.mergeConflictInfoList = mergeConflictInfoList;
	}

	/**
	 * Main function of this class. It build merge scenarios from the database,
	 * rebuild merge conflicts and save it in the database
	 */
	public void storer() {
		try {

			Logger.log(log, "MergeConflictInfo creation start.");

			String chunkHasConflict = " and chunks.has_conflict=1";
			List<MergeScenario> mergeScenariosList = mergeDao.getMergeScenariosFromDataBase(project.getIdDB(),
					chunkHasConflict);

			for (MergeScenario ms : mergeScenariosList) {
				buildMergeConflictInfo(ms);
			}

			// As merge_conflict_info database rows are normally very large, we set each
			// instance individually (i.e., one row per query)
			for (MergeConflictInfo mci : mergeConflictInfoList) {
				DBWriter.INSTANCE.persistMergeConflictCodeDB(mci);
			}

			Logger.log(log, "MergeConflictInfo creation finished.");

		} catch (IOException | SQLException | InvalidBeanException e) {
			Logger.logStackTrace(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is the function that indeed build merge conflicts info. The time is in
	 * seconds
	 *
	 * @param newMergeScenario
	 * @throws IOException
	 */
	private void buildMergeConflictInfo(MergeScenario newMergeScenario) throws IOException {

		git.exec(getProject().getRepository().getDir(), "reset", "--hard");
		git.exec(getProject().getRepository().getDir(), "checkout ", "-f", newMergeScenario.getMergeCommitHash());

		ArrayList<FileMS> fileList = new ArrayList<>();

		Optional<Commit> mergeCommit = getProject().getRepository().getCommit(newMergeScenario.getMergeCommitHash());

		// Re-build merge scenario
		if (mergeCommit.isPresent()) {
			newMergeScenario.setMergeCommit(mergeCommit.get());
			newMergeScenario.setLeftParentCommit(mergeCommit.get().getParents().get().get(0));
			newMergeScenario.setRightParentCommit(mergeCommit.get().getParents().get().get(1));

			Optional<Commit> base = newMergeScenario.getLeftParentCommit()
					.getMergeBase(newMergeScenario.getRightParentCommit());
			if (base.isPresent()) {
				newMergeScenario.setBaseCommit(base.get());
			}

			Optional<Status> isConflicted = newMergeScenario.getLeftParentCommit()
					.merge(newMergeScenario.getRightParentCommit());

			if (isConflicted.isPresent() && isConflicted.get().hasConflicts()) {

				// it is made for all files of a merge scenario
				for (Path file : mergeBuilder.getAffectedPaths(newMergeScenario, true)) {

					FileMS newFile = new FileMS(file);

					Optional<List<MergeConflict>> blameLineListConflictedMerge = getProject().getRepository()
							.blameUnmergedFile(file);

					if (blameLineListConflictedMerge.isPresent()) {

						for (MergeConflict merge : blameLineListConflictedMerge.get()) {
							List<BlameLine> listLeft = merge.left;
							List<BlameLine> listRight = merge.right;
							List<Commit> commitList = new ArrayList<Commit>();

							Chunk<Object> newChunk = new Chunk<Object>();

							Integer countLines = 0;
							if (listLeft.isEmpty()) {
								newChunk.setBeginLine(1);
							} else {
								newChunk.setBeginLine(listLeft.get(0).finalLineNumber - 1);
								countLines = listLeft.get(0).finalLineNumber - 1;
							}

							// get commits between the base and the merge commit
							// LEFT SIDE
							StringBuilder leftCode = new StringBuilder("");
							StringBuilder rightCode = new StringBuilder("");
							for (BlameLine line : listLeft) {
								if (!commitList.contains(line.commit)
										&& (line.commit.checkAncestry(newMergeScenario.getBaseCommit()).orElse(false))
										&& (!line.commit.equals(newMergeScenario.getBaseCommit()))) {
									commitList.add(line.commit);
								}
								leftCode.append(line.line);
								leftCode.append(" \n");
								countLines++;
							}
							newChunk.setLeftCode(leftCode);
							countLines++;

							// RIGHT SIDE
							for (BlameLine line : listRight) {
								if (!commitList.contains(line.commit)
										&& (line.commit.checkAncestry(newMergeScenario.getBaseCommit()).orElse(false))
										&& (!line.commit.equals(newMergeScenario.getBaseCommit()))) {
									commitList.add(line.commit);
								}
								rightCode.append(line.line);
								rightCode.append(" \n");
								countLines++;
							}

							if (!commitList.contains(newMergeScenario.getMergeCommit())) {
								commitList.add(newMergeScenario.getMergeCommit());
							}

							// to avoid having two lists we set all commits of a chunk in the left side
							newChunk.setLeftCommitList(commitList);
							newChunk.setRightCode(rightCode);
							countLines++;
							newChunk.setEndLine(countLines);

							// Double check. In a specific case it can be considered
							// a chunk conflict, but it can actually not be.
							if (!newChunk.getLeftCode().toString().isEmpty()
									&& !newChunk.getRightCode().toString().isEmpty()) {
								newMergeScenario.setHasConflict(true);
								newChunk.isConflict(true);
								newFile.setConflict(true);
							}

							newFile.getChunkList().add(newChunk);
						}
					}

					if (!newFile.getChunkList().isEmpty()) {
						fileList.add(newFile);
					}
				}
			}

			// Get the difference of time between merge and both parent commits
			try {

				Integer leftParentCommitId = commitDao.getByHash(newMergeScenario.getLeftParentCommit().getId());
				Integer rightParentCommitId = commitDao.getByHash(newMergeScenario.getRightParentCommit().getId());

				if (leftParentCommitId == null) {
					DBWriter.INSTANCE.persistCommitAndRelatedData(getProject().getIdDB(),
							newMergeScenario.getLeftParentCommit());
					leftParentCommitId = commitDao.getByHash(newMergeScenario.getLeftParentCommit().getId());
				}
				if (rightParentCommitId == null) {
					DBWriter.INSTANCE.persistCommitAndRelatedData(getProject().getIdDB(),
							newMergeScenario.getRightParentCommit());
					rightParentCommitId = commitDao.getByHash(newMergeScenario.getRightParentCommit().getId());
				}

				long leftDiff = ChronoUnit.SECONDS.between(newMergeScenario.getLeftParentCommit().getAuthorTime(),
						newMergeScenario.getMergeCommit().getAuthorTime());
				long rightDiff = ChronoUnit.SECONDS.between(newMergeScenario.getRightParentCommit().getAuthorTime(),
						newMergeScenario.getMergeCommit().getAuthorTime());

				for (FileMS file : newMergeScenario.getListFileMS()) {
					for (FileMS file2 : fileList) {
						if (file.getFileName().equals(file2.getFileName())) {
							for (Chunk<Object> chunk : file.getChunkList()) {
								for (Chunk<Object> chunk2 : file2.getChunkList()) {
									if (chunk.getBeginLine().equals(chunk2.getBeginLine())
											&& chunk.getEndLine().equals(chunk2.getEndLine())) {
										MergeConflictInfo mergeConflictInfo = new MergeConflictInfo(chunk.getIdDB(),
												leftParentCommitId, rightParentCommitId,
												cleanCode(chunk2.getLeftCode().toString()),
												cleanCode(chunk2.getRightCode().toString()), null,
												leftDiff, rightDiff, chunk2.getLeftCommitList());

										mergeConflictInfoList.add(mergeConflictInfo);
									}
								}
							}
						}
					}
				}

			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
				Logger.log(log, "ERROR IT BROKE WHEN GETTING THE COMMITS TIME DIFF.");
			}
		} else {
			// TODO: Investigate the reason why some merge commits are failing to checkout
			System.out.println("Fail to checkout this commit: " + newMergeScenario.getMergeCommitHash());
		}
	}

	// Remove special characters from the code
	private String cleanCode(String code) {
		char ch;
		// search for strange char in the String
		for (int i = 0; i < code.length(); i += 1) {
			ch = code.charAt(i);
			// type 19 has the strange symbols
			if (Character.getType(ch) == 19) {
				if (code.length() == 1) {
					code = "";
				} else {
					code = code.substring(0, i) + code.substring(i + 1);
					i--;
				}
			}
		}
		return code;
	}
}
