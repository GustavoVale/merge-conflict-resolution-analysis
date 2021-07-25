package commnet.metricsExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gitwrapper.repo.GitWrapper;
import commnet.model.beans.FileMetrics;
import commnet.model.beans.Project;
import commnet.model.dao.ChunkDao;
import commnet.model.dao.CommitDao;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.FileDao;
import commnet.model.dao.FileMetricDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class FileMetricsExtractor {

	private File log;
	private Project project;

	private FileDao fileDao = new FileDao();
	private FileMetricDao fileMetricDao = new FileMetricDao();
	private ChunkDao<Object> chunkDao = new ChunkDao<Object>();
	private CommitDao commitDao = new CommitDao();
	private DeveloperNodeDao devDao = new DeveloperNodeDao();

	private GitWrapper git;

	public FileMetricsExtractor(Project project, File log) {
		setProject(project);
		setLogFile(log);
		git = getProject().getRepository().getGit();
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

	public void extract() {
		try {

			git.exec(getProject().getRepository().getDir(), "reset", "--hard");

			List<Integer> fileIdList = fileDao.getFileListByProject(project.getIdDB());
			List<Integer> fileMetricsIdList = fileMetricDao.getFileMetricsFileIdListByProject(project.getIdDB());

			if (fileIdList.size() != fileMetricsIdList.size()) {
				for (Integer fileId : fileMetricsIdList) {
					fileIdList.remove(fileId);
				}

				computeFileMetrics(fileIdList);
			}
		} catch (SQLException | InvalidBeanException | InterruptedException | IOException e1) {
			Logger.log(log, "ERROR COMPUTING FILE METRICS.");
			e1.printStackTrace();
		}
	}

	private void computeFileMetrics(List<Integer> fileIdList)
			throws InvalidBeanException, SQLException, InterruptedException, IOException {

		List<FileMetrics> fileMetricsList = new ArrayList<>();
		for (Integer fileId : fileIdList) {
			FileMetrics fileMetrics = new FileMetrics(fileId);

			fileMetrics.setNumberChunks(chunkDao.getListOfChunksInFileMS(fileId).size());
			fileMetrics.setNumberConflictedChunks(chunkDao.getNumberConflictedChunkByFile(fileId));
			fileMetrics.setNumberLeftChunks(chunkDao.getNumberChunkByFileAndSide(fileId, "left"));
			fileMetrics.setNumberRightChunks(chunkDao.getNumberChunkByFileAndSide(fileId, "right"));

			fileMetrics.setNumberCommits(commitDao.getCommitsByFile(fileId));
			fileMetrics.setNumberLeftCommits(commitDao.getCommitsByFileAndSide(fileId, "left"));
			fileMetrics.setNumberRightCommits(commitDao.getCommitsByFileAndSide(fileId, "right"));

			fileMetrics.setNumberDevelopers(devDao.getDevByFile(fileId));
			List<Integer> leftDevs = devDao.getDevByFileAndSide(fileId, "left");
			fileMetrics.setNumberLeftDevelopers(leftDevs.size());
			List<Integer> rightDevs = devDao.getDevByFileAndSide(fileId, "right");
			fileMetrics.setNumberRightDevelopers(rightDevs.size());
			fileMetrics.setNumberBothSideDevelopers(getIntersection(leftDevs, rightDevs));

			fileMetrics.setLoc(getLOC(fileDao.getMapFilePathAndMergeCommitHash(fileId)));
			fileMetricsList.add(fileMetrics);
		}

		DBWriter.INSTANCE.persistFileMetrics(fileMetricsList);

	}

	/// Arguments:
	/// HashMap key is the filepath we need to count loc.
	/// HashMap value is the hash of the commit to checkout.
	private Integer getLOCbyCLOC(HashMap<String, String> filepathAndMergeCommitHash) throws InterruptedException, IOException {
		Integer sb = 0;

		HashMap.Entry<String,String> entry = filepathAndMergeCommitHash.entrySet().iterator().next();
		 String key= entry.getKey();
		 String value=entry.getValue();

		git.exec(getProject().getRepository().getDir(), "checkout", value);

		ProcessBuilder pb = new ProcessBuilder("cloc", "./CN-files/repos/" + getProject().getName() + "/" + key,
				"--quiet");
		Process process;
		process = pb.start();
		int errCode = process.waitFor();
		if (errCode == 0) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				int count = 0;
				while ((line = br.readLine()) != null) {
					if (count == 2) {
						line = line.replaceAll(".* ", "");
						sb = Integer.parseInt(line);
						count = 0;
					}
					if (line.startsWith("-----")) {
						count++;
					}
				}
			} finally {
				br.close();
			}
		}
		return sb;
	}

	/// Arguments:
	/// HashMap key is the filepath we need to count loc.
	/// HashMap value is the hash of the commit to checkout.
	private Integer getLOC(HashMap<String, String> filepathAndMergeCommitHash) throws IOException {
		HashMap.Entry<String,String> entry = filepathAndMergeCommitHash.entrySet().iterator().next();
		String key= entry.getKey();
		String value=entry.getValue();
		
		git.exec(getProject().getRepository().getDir(), "checkout", value);
		String filePath = new String("./CN-files/repos/" + getProject().getName() + "/" + key);
		File tmpDir = new File(filePath);
		boolean exists = tmpDir.exists();
		
		if(!exists || tmpDir.isDirectory()) {
			return 0;
		}
		
		BufferedReader reader = new BufferedReader(new FileReader("./CN-files/repos/" + getProject().getName() + "/" + key));
		int lines = 0;
		while (reader.readLine() != null) lines++;
		reader.close();
				
		return lines;
	}

	private Integer getIntersection(List<Integer> leftFiles, List<Integer> rightFiles) {
		List<Integer> result = new ArrayList<>();

		if (leftFiles.size() != 0 && rightFiles.size() != 0) {
			for (Integer fileId1 : leftFiles) {
				for (Integer fileId2 : rightFiles) {
					if (fileId1.equals(fileId2)) {
						result.add(fileId1);
						break;
					}
				}
			}
		}
		return result.size();
	}

}
