package commnet.metricsExtractor;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.ChunkMetrics;
import commnet.model.beans.Project;
import commnet.model.dao.ChunkDao;
import commnet.model.dao.ChunkMetricDao;
import commnet.model.dao.CommitDao;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class ChunkMetricsExtractor {

	private File log;
	private Project project;

	private ChunkDao<Object> chunkDao = new ChunkDao<Object>();
	private ChunkMetricDao chunkMetricDao = new ChunkMetricDao();
	private CommitDao commitDao = new CommitDao();
	private DeveloperNodeDao devDao = new DeveloperNodeDao();

	public ChunkMetricsExtractor(Project project, File log) {
		setProject(project);
		setLogFile(log);
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

			List<Integer> chunkIdList = chunkDao.getChunkListByProject(project.getIdDB());
			List<Integer> chunkMetricsIdList = chunkMetricDao.getChunkMetricsChunkIdListByProject(project.getIdDB());

			if (chunkIdList.size() != chunkMetricsIdList.size()) {
				for (Integer chunkId : chunkMetricsIdList) {
					chunkIdList.remove(chunkId);
				}

				computeChunkMetrics(chunkIdList);
			}
		} catch (SQLException | InvalidBeanException e1) {
			Logger.log(log, "ERROR COMPUTING FILE METRICS.");
			e1.printStackTrace();
		}
	}

	private void computeChunkMetrics(List<Integer> chunkIdList) throws InvalidBeanException, SQLException {
		List<ChunkMetrics> chunkMetricsList = new ArrayList<>();

		for (Integer chunkId : chunkIdList) {
			ChunkMetrics chunkMetrics = new ChunkMetrics(chunkId);

			chunkMetrics.setNumberCommits(commitDao.getCommitsByChunk(chunkId));
			chunkMetrics.setNumberLeftCommits(commitDao.getCommitsByChunkAndSide(chunkId, "left"));
			chunkMetrics.setNumberRightCommits(commitDao.getCommitsByChunkAndSide(chunkId, "right"));

			chunkMetrics.setNumberDevelopers(devDao.getDevByChunk(chunkId));
			List<Integer> leftDevs = devDao.getDevByChunkAndSide(chunkId, "left");
			chunkMetrics.setNumberLeftDevelopers(leftDevs.size());
			List<Integer> rightDevs = devDao.getDevByChunkAndSide(chunkId, "right");
			chunkMetrics.setNumberRightDevelopers(rightDevs.size());
			chunkMetrics.setNumberBothSideDevelopers(getIntersection(leftDevs, rightDevs));

			chunkMetricsList.add(chunkMetrics);

		}

		DBWriter.INSTANCE.persistChunkMetrics(chunkMetricsList);

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
