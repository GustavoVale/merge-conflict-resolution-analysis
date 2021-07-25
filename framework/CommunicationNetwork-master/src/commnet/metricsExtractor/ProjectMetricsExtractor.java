package commnet.metricsExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import commnet.model.beans.Project;
import commnet.model.beans.ProjectMetrics;
import commnet.model.dao.ChunkDao;
import commnet.model.dao.CommitDao;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.FileDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ProjectMetricsExtractor {

	private Project project;

	private MergeScenarioDao msDao = new MergeScenarioDao();
	private FileDao fileDao = new FileDao();
	private ChunkDao<Object> chunkDao = new ChunkDao<>();
	private CommitDao commitDao = new CommitDao();
	private DeveloperNodeDao devDao = new DeveloperNodeDao();

	protected File log;

	public ProjectMetricsExtractor(Project project, File log) {
		setProject(project);
		setLogFile(log);
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

	public void extract() {
		try {
			ProjectMetrics projectMetrics = new ProjectMetrics(project.getIdDB());

			try {
				projectMetrics.setLoc(getLOCbyCLOC());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			projectMetrics.setMsComputed(msDao.getMSListByProject(getProject().getIdDB()).size());
			projectMetrics.setMsIgnored(getNumberIgnoredMS());
			projectMetrics.setMsConflicted(msDao.getConflictedMSByProject(getProject().getIdDB()));
			projectMetrics.setNumberOfFiles(fileDao.getFileListByProject(getProject().getIdDB()).size());
			projectMetrics.setNumberOfChunks(chunkDao.getChunkListByProject(getProject().getIdDB()).size());
			projectMetrics.setNumberOfCommits(commitDao.getNumberOfCommitsByProject(getProject().getIdDB()));
			projectMetrics.setNumberOfDevelopers(devDao.getNumberDevByProject(getProject().getIdDB()));
			projectMetrics.setNumberOfDistinctFiles(fileDao.getDistinctFileNumberByProject(getProject().getIdDB()));

			DBWriter.INSTANCE.persistProjectMetrics(projectMetrics);

		} catch (SQLException | InvalidBeanException | IOException e1) {
			Logger.log(log, "ERROR COMPUTING NET METRICS.");
			e1.printStackTrace();
		}
	}

	private Integer getLOCbyCLOC() throws InterruptedException, IOException {
		Integer sb = 0;
		ProcessBuilder pb = new ProcessBuilder("cloc", "./CN-files/repos/" + getProject().getName(), "--quiet");
		Process process;
		process = pb.start();
		int errCode = process.waitFor();
		if (errCode == 0) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("SUM:")) {
						line = line.replaceAll(".* ", "");
						sb = Integer.parseInt(line);
					}
				}
			} finally {
				br.close();
			}
		}
		return sb;
	}

	private Integer getNumberIgnoredMS() {

		int count = 0;
		BufferedReader br;
		try {
			br = new BufferedReader(
					new FileReader("./CN-files/unused-commits/" + getProject().getName() + "_UMCFile.xls"));

			while ((br.readLine()) != null) {
				count++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;

	}

}
