package commnet.crawler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gitwrapper.process.ToolNotWorkingException;
import gitwrapper.repo.Commit;
import gitwrapper.repo.GitWrapper;
import gitwrapper.repo.Repository;
import commnet.model.beans.CommitN;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.Project;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.db.DBWriter;
import commnet.model.enums.ExtraMode;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Directories;
import commnet.util.Logger;

public class NewRepCrawler implements Runnable {

	private GitWrapper git;
	private Project project;
	private Repository repository;

	private List<CommitN> commitList;
	private List<String> extraData;
	private ExtraMode extraMode;

	public NewRepCrawler(String url, List<String> extraFile, ExtraMode extraMode) throws IOException {
		project = new Project(url);
		this.extraData = extraFile;
		this.extraMode = extraMode;

		if (extraMode.equals(ExtraMode.COMMITUPDATER) || extraMode.equals(ExtraMode.COMMITFIELDSUPDATER)) {
			// clone and setup repository
			try {
				git = new GitWrapper("git");
				Optional<Repository> optional = git.clone(Directories.getReposDir(), url, false);
				if (optional.isPresent()) {
					repository = optional.get();
					project.setRepository(repository);
				}
			} catch (ToolNotWorkingException e) {
				Logger.logStackTrace(e);
				throw new RuntimeException(e);
			}
		}
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public java.util.List<CommitN> getCommitList() {
		return commitList;
	}

	public void setCommitList(java.util.List<CommitN> commitList) {
		this.commitList = commitList;
	}

	@Override
	public void run() {

		try {
			DBWriter.INSTANCE.persistProject(getProject());
		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
		}

		if (extraMode.equals(ExtraMode.COMMITUPDATER)) {
			try {
				setCommitList(DBWriter.INSTANCE.getCommitListFromDB(project.getIdDB()));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			for (CommitN commit : getCommitList()) {
				Optional<Commit> commitByHash = getProject().getRepository().getCommit(commit.getHash());
				if (commitByHash.isPresent()) {
					commit.setCommitDate(commitByHash.get());
				}
			}

			try {
				DBWriter.INSTANCE.updateCommitDateInDB(getCommitList());
			} catch (SQLException | InvalidBeanException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.EDGEREMOVER)) {
			List<Integer> edgeTypeList = new ArrayList<>();
			for (String str : extraData) {
				edgeTypeList.add(Integer.parseInt(str));
			}

			try {
				DBWriter.INSTANCE.deleteSpecificEdgeTypesByProject(project.getIdDB(), edgeTypeList);
			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.DEVELOPERREMOVER)) {
			List<Integer> devList = new ArrayList<>();
			for (String str : extraData) {
				devList.add(Integer.parseInt(str));
			}

			for (Integer devIdDB : devList) {
				try {
					DBWriter.INSTANCE.deleteContributorData(devIdDB);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		if (extraMode.equals(ExtraMode.ISSUEREMOVER)) {
			try {
				DBWriter.INSTANCE.deleteIssueData(getProject().getIdDB());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.NONUPDATEDCOMMITRETRIEVER)) {
			try {
				List<CommitN> newCommitList = new ArrayList<>();

				List<CommitN> commitList = DBWriter.INSTANCE.getCommitListFromDB(getProject().getIdDB());

				for (CommitN commit : commitList) {

					commit.updateCommitDate();

					List<String> newString = commit.getTimeFromGMTString();

					if (newString.get(0).equals("00") && newString.get(1).equals("00")
							&& newString.get(2).equals("00")) {
						newCommitList.add(commit);
					}
				}

				if (!newCommitList.isEmpty()) {
					File nonUpdatedCommitsFile = new File(Directories.getNonUpdatedCommitsDir(),
							project.getName() + ".xls");
					for (CommitN commit : newCommitList) {
						Logger.log(nonUpdatedCommitsFile,
								commit.getIdDB() + ";" + commit.getHash() + ";" + commit.getContIdDB());
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.PROJECTMETRICREMOVER)) {
			try {
				DBWriter.INSTANCE.deleteProjectMetricData(getProject().getIdDB());
			} catch (SQLException | InvalidBeanException e) {
				e.printStackTrace();
			}
		}
		
		if (extraMode.equals(ExtraMode.NETMETRICREMOVER)) {
			try {
				DBWriter.INSTANCE.deleteNetworkMetricData(getProject().getIdDB(), "net_metrics");
			} catch (SQLException | InvalidBeanException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.MSMETRICREMOVER)) {
			try {
				DBWriter.INSTANCE.deleteMSMetricData(getProject().getIdDB(), "ms_metrics");
			} catch (SQLException | InvalidBeanException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.FILEMETRICREMOVER)) {
			try {
				DBWriter.INSTANCE.deleteFileMetricData(getProject().getIdDB(), "file_metrics");
			} catch (SQLException | InvalidBeanException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.CHUNKMETRICREMOVER)) {
			try {
				DBWriter.INSTANCE.deleteChunkMetricData(getProject().getIdDB(), "chunk_metrics");
			} catch (SQLException | InvalidBeanException e) {
				e.printStackTrace();
			}
		}

		if (extraMode.equals(ExtraMode.COMMITFIELDSUPDATER)) {
			try {
				setCommitList(DBWriter.INSTANCE.getCommitListFromDB(project.getIdDB()));

				DeveloperNodeDao dndao = new DeveloperNodeDao();
				List<CommitN> commitList = new ArrayList<>();

				for (CommitN commit : getCommitList()) {
					if (commit.getCommitterIdDB().equals(0)) {
						Optional<Commit> commitByHash = getProject().getRepository().getCommit(commit.getHash());
						if (commitByHash.isPresent()) {
							Integer committerIdDB = dndao.getDevIdDB(new DeveloperNode(null,
									commitByHash.get().getCommitter(), commitByHash.get().getCommitterMail()));
							commit.setCommitterIdDB(committerIdDB);
							commitList.add(commit);
						}
					}
				}

				DBWriter.INSTANCE.updateCommitFieldsInDB(commitList);

			} catch (InvalidBeanException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.gc();
	}

}
