package commnet.crawler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import githubinterface.GitHubRepository;
import gitwrapper.process.ToolNotWorkingException;
import gitwrapper.repo.GitWrapper;
import gitwrapper.repo.Repository;
import commnet.builder.CommitterBuilder;
import commnet.builder.CommunicatorsBuilder;
import commnet.builder.DevBuilder;
import commnet.builder.IntegratorBuilder;
import commnet.builder.IssueBuilder;
import commnet.builder.MergeConflictInfoBuilder;
import commnet.builder.MergeScenarioBuilder;
import commnet.builder.NetworkBuilder;
import commnet.metricsExtractor.ChunkMetricsExtractor;
import commnet.metricsExtractor.FileMetricsExtractor;
import commnet.metricsExtractor.MergeConflictMetricsExtractor;
import commnet.metricsExtractor.MergeScenarioMetricsExtractor;
import commnet.metricsExtractor.NetworkMetricsExtractor;
import commnet.metricsExtractor.ProjectMetricsExtractor;
import commnet.model.beans.Project;
import commnet.model.db.DBWriter;
import commnet.model.enums.NetworkType;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Directories;
import commnet.util.Logger;

public class RepositoryCrawler implements Runnable {

	private GitWrapper git;
	private Project project;
	private Repository repository;
	private File log;
	private File unusedMergeCommitsFile;
	private List<String> token;
	private List<String> mergeCommits;
	private NetworkType netType;

	private IssueBuilder issues;
	private MergeScenarioBuilder mergeScenarios;

	public RepositoryCrawler(String url, List<String> tokens, List<String> mergeCommits, NetworkType netType)
			throws IOException {
		project = new Project(url);
		this.token = tokens;
		this.mergeCommits = mergeCommits;
		this.netType = netType;
		setUnusedMergeCommitsFile(new File(Directories.getUnusedMergeCommitsDir(), project.getName() + "_UMCFile.xls"));
		setLogFile(new File(Directories.getLogDir(), "thread-" + project.getName() + ".log"));

		// clone and setup repository
		try {
			git = new GitWrapper("git");
			if (netType.equals(NetworkType.MERGESCENARIO) || netType.equals(NetworkType.ISSUE)
					|| netType.equals(NetworkType.MERGECONFLICTINFO)) {
				Optional<Repository> optional = git.clone(Directories.getReposDir(), url, true);
				if (optional.isPresent()) {
					repository = optional.get();
					project.setRepository(repository);
				}
			} else {
				Optional<Repository> optional = git.clone(Directories.getReposDir(), url, false);
				if (optional.isPresent()) {
					repository = optional.get();
					project.setRepository(repository);
				}
			}
		} catch (ToolNotWorkingException e) {
			Logger.logStackTrace(e);
			throw new RuntimeException(e);
		}
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setLogFile(File f) {
		this.log = f;
	}

	public void setUnusedMergeCommitsFile(File f) {
		this.unusedMergeCommitsFile = f;
	}

	public void setRepositoryDir(File directory) {
		this.setRepositoryDir(directory);
	}

	@Override
	public void run() {

		// check database connection and set projectIdDB
		try {
			DBWriter.INSTANCE.setLogFile(log);
			DBWriter.INSTANCE.persistProject(getProject());
		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
			Logger.log(log, "ERROR PERSISTING PROJECT in DATABASE");
		}

		// Create thread to run MergeScenarioBuilder
		NCThreadPoolExecutor poolMS = new NCThreadPoolExecutor();
		// Create thread to run IssueBuilder
		NCThreadPoolExecutor poolIssue = new NCThreadPoolExecutor();

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.CONTCOMNETWORKS) || netType.equals(NetworkType.CONTRIBUTIONNETWORKS)
				|| netType.equals(NetworkType.COMPREHENSIVENETWORKS) || netType.equals(NetworkType.PRECISENETWORKS)
				|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)
				|| netType.equals(NetworkType.COMMUNICATIONNETWORKS) || netType.equals(NetworkType.MERGESCENARIO)) {
			boolean simpleDatabaseSearch = false;
			if (netType.equals(NetworkType.NETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)
					|| netType.equals(NetworkType.CONTRIBUTIONNETWORKS)
					|| netType.equals(NetworkType.COMPREHENSIVENETWORKS) || netType.equals(NetworkType.PRECISENETWORKS)
					|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)
					|| netType.equals(NetworkType.COMMUNICATIONNETWORKS)) {
				simpleDatabaseSearch = true;
			}

			// This block build mergeScenarios
			mergeScenarios = new MergeScenarioBuilder(project, simpleDatabaseSearch, mergeCommits);
			mergeScenarios.setLogFile(log);
			mergeScenarios.setUnusedMCFile(unusedMergeCommitsFile);
			poolMS.runTask(mergeScenarios);
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.CONTCOMNETWORKS) || netType.equals(NetworkType.COMPREHENSIVENETWORKS)
				|| netType.equals(NetworkType.PRECISENETWORKS) || netType.equals(NetworkType.COMMUNICATIONNETWORKS)
				|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS) || netType.equals(NetworkType.ISSUE)) {
			boolean simpleDatabaseSearch = false;
			if (netType.equals(NetworkType.NETWORKS) || netType.equals(NetworkType.CONTCOMNETWORKS)
					|| netType.equals(NetworkType.COMPREHENSIVENETWORKS) || netType.equals(NetworkType.PRECISENETWORKS)
					|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)
					|| netType.equals(NetworkType.COMMUNICATIONNETWORKS)) {
				simpleDatabaseSearch = true;
			}
			GitHubRepository gitHubRepo = new GitHubRepository(project.getRepository(), git, token);
			// This block build issues
			issues = new IssueBuilder(gitHubRepo, project, simpleDatabaseSearch);
			issues.setLogFile(log);
			poolIssue.runTask(issues);
		}

		// Threads stop to get tasks and wait until the remainder tasks be
		// finished
		poolMS.shutDown();
		poolIssue.shutDown();
		poolMS.waitFinish();
		poolIssue.waitFinish();

		if (netType.equals(NetworkType.CONTCOMNETWORKS) || netType.equals(NetworkType.CONTRIBUTIONNETWORKS)
				|| netType.equals(NetworkType.NETWORKS)) {
			if (mergeScenarios == null) {
				Logger.log(
						"YOU ARE TRYING TO GET MERGE SCENARIOS FROM THE DATABASE, BUT IT DOES NOT HAVE ANY MERGE SCENARIOS YET. BE AWARE OF IT ");
			}
		}

		if (netType.equals(NetworkType.COMMUNICATIONNETWORKS) || netType.equals(NetworkType.COMPREHENSIVENETWORKS)
				|| netType.equals(NetworkType.CONTCOMNETWORKS) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.PRECISENETWORKS) || netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)) {
			if (issues == null) {
				Logger.log(
						"YOU ARE TRYING TO GET COMMUNICATION FROM THE DATABASE, BUT IT DOES NOT HAVE ANY ISSUES YET ");
			}
		}

		if (mergeScenarios == null) {
			mergeScenarios = new MergeScenarioBuilder(getProject(), false, null);
		}
		if (issues == null) {
			issues = new IssueBuilder(null, null, false);
		}

		project.setIssueList(issues.getIssuesList());

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.NETWORKS)
				|| netType.equals(NetworkType.CONTCOMNETWORKS) || netType.equals(NetworkType.COMPREHENSIVENETWORKS)
				|| netType.equals(NetworkType.PRECISENETWORKS) || netType.equals(NetworkType.CONTRIBUTIONNETWORKS)
				|| netType.equals(NetworkType.COMMUNICATIONNETWORKS)
				|| netType.equals(NetworkType.CHANGEDARTIFACTNETWORKS)) {
			// Build Networks based on mergeScenarios and Issues depending on
			// the mode chosen
			NetworkBuilder network = new NetworkBuilder(getProject(), mergeScenarios.getMergeScenariosList(),
					issues.getIssuesList(), netType);
			network.setLogFile(log);
			network.build();
			// network.print();
		}

		if (netType.equals(NetworkType.ALLMETRICS) || netType.equals(NetworkType.PROJECTMETRICS)
				|| netType.equals(NetworkType.NETWORKMETRICS) || netType.equals(NetworkType.MERGEMETRICS)
				|| netType.equals(NetworkType.FILEMETRICS) || netType.equals(NetworkType.CHUNKMETRICS)) {
			if (netType.equals(NetworkType.ALLMETRICS) || netType.equals(NetworkType.PROJECTMETRICS)) {
				ProjectMetricsExtractor projectMetricExtractor = new ProjectMetricsExtractor(getProject(), log);
				projectMetricExtractor.extract();
			}
			if (netType.equals(NetworkType.ALLMETRICS) || netType.equals(NetworkType.NETWORKMETRICS)) {
				NetworkMetricsExtractor netMetricExtractor = new NetworkMetricsExtractor(getProject(), log);
				netMetricExtractor.extract();
			}
			if (netType.equals(NetworkType.ALLMETRICS) || netType.equals(NetworkType.MERGEMETRICS)) {
				MergeScenarioMetricsExtractor msMetricsExtrator = new MergeScenarioMetricsExtractor(getProject(), log);
				msMetricsExtrator.extract();
			}
			if (netType.equals(NetworkType.ALLMETRICS) || netType.equals(NetworkType.FILEMETRICS)) {
				FileMetricsExtractor fileMetricsExtrator = new FileMetricsExtractor(getProject(), log);
				fileMetricsExtrator.extract();
			}
			if (netType.equals(NetworkType.ALLMETRICS) || netType.equals(NetworkType.CHUNKMETRICS)) {
				ChunkMetricsExtractor chunkMetricsExtrator = new ChunkMetricsExtractor(getProject(), log);
				chunkMetricsExtrator.extract();
			}
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.COMPREHENSIVECOMMUNICATORS)
				|| netType.equals(NetworkType.PRECISECOMMUNICATORS)
				|| netType.equals(NetworkType.CHANGEDARTIFACTCOMMUNICATORS)) {
			if (!netType.equals(NetworkType.ALL)) {
				CommunicatorsBuilder devRoleBuilder = new CommunicatorsBuilder(getProject().getIdDB(), netType, log);
				devRoleBuilder.builder();
			} else {
				CommunicatorsBuilder devRoleBuilder = new CommunicatorsBuilder(getProject().getIdDB(),
						NetworkType.COMPREHENSIVECOMMUNICATORS, log);
				devRoleBuilder.builder();
				devRoleBuilder = new CommunicatorsBuilder(getProject().getIdDB(), NetworkType.PRECISECOMMUNICATORS,
						log);
				devRoleBuilder.builder();
				devRoleBuilder = new CommunicatorsBuilder(getProject().getIdDB(),
						NetworkType.CHANGEDARTIFACTCOMMUNICATORS, log);
				devRoleBuilder.builder();
			}

		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.DEVS)) {
			DevBuilder devBuilder = new DevBuilder(getProject());
			devBuilder.storer();
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.COMMITTERS)) {
			CommitterBuilder committerBuilder = new CommitterBuilder(getProject());
			committerBuilder.storer();
		}

		if (netType.equals(NetworkType.ALL) || netType.equals(NetworkType.INTEGRATORS)) {
			IntegratorBuilder integratorBuilder = new IntegratorBuilder(getProject());
			integratorBuilder.storer();
		}

		if (netType.equals(NetworkType.MERGECONFLICTINFO)) {
			MergeConflictInfoBuilder mergeConflictInfoBuilder = new MergeConflictInfoBuilder(getProject(), log);
			mergeConflictInfoBuilder.storer();
		}

		if (netType.equals(NetworkType.MERGECONFLICTMETRICS)) {
			MergeConflictMetricsExtractor mergeConflictMetricsBuilder = new MergeConflictMetricsExtractor(getProject());
			mergeConflictMetricsBuilder.storer();
		}

		System.gc();
	}
}
