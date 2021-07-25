package commnet.metricsExtractor;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.Chunk;
import commnet.model.beans.MergeScenarioMetrics;
import commnet.model.beans.Project;
import commnet.model.dao.ChunkDao;
import commnet.model.dao.CommitDao;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.FileDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.dao.MergeScenarioMetricDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class MergeScenarioMetricsExtractor {

	private File log;
	private Project project;

	private MergeScenarioDao msDao = new MergeScenarioDao();
	private MergeScenarioMetricDao msMetricDao = new MergeScenarioMetricDao();
	private FileDao fileDao = new FileDao();
	private ChunkDao<Object> chunkDao = new ChunkDao<Object>();
	private CommitDao commitDao = new CommitDao();
	private DeveloperNodeDao devDao = new DeveloperNodeDao();

	public MergeScenarioMetricsExtractor(Project project, File log) {
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

			List<Integer> msIdList = msDao.getMSListByProject(project.getIdDB());
			List<Integer> msMetricsIdList = msMetricDao.getMsMetricsListMergeScenarioIdByProject(project.getIdDB());

			if (msIdList.size() != msMetricsIdList.size()) {
				for (Integer msMetricId : msMetricsIdList) {
					msIdList.remove(msMetricId);
				}

				computeMsMetrics(msIdList);
			}
		} catch (SQLException | InvalidBeanException e1) {
			Logger.log(log, "ERROR COMPUTING MS METRICS.");
			e1.printStackTrace();
		}
	}

	private void computeMsMetrics(List<Integer> msIdList) throws InvalidBeanException, SQLException {
		List<MergeScenarioMetrics> msMetricsList = new ArrayList<>();
		
		for (Integer msId : msIdList) {
			MergeScenarioMetrics msMetrics = new MergeScenarioMetrics(msId);

			msMetrics.setNumberFiles(fileDao.getListOfFilesInMS(msId).size());
			msMetrics.setNumberConflictedFiles(fileDao.getNumberOfConflictedFilesInMS(msId));
			List<Integer> leftFiles = fileDao.getNumberFilebyMsAndSide(msId, "left");
			msMetrics.setNumberLeftFiles(leftFiles.size());
			List<Integer> rightFiles = fileDao.getNumberFilebyMsAndSide(msId, "right");
			msMetrics.setNumberRightFiles(rightFiles.size());
			msMetrics.setNumberBothSideFiles(getIntersection(leftFiles, rightFiles));

			msMetrics.setNumberChunks(chunkDao.getNumberOfChunksInMS(msId));
			msMetrics.setNumberConflictedChunks(chunkDao.getNumberOfConflictedChunksInMS(msId));
			msMetrics.setNumberLeftChunks(chunkDao.getNumberChunksbySide(msId, "left").size());
			msMetrics.setNumberRightChunks(chunkDao.getNumberChunksbySide(msId, "right").size());

			msMetrics.setNumberCommits(commitDao.getCommitsByMs(msId));
			msMetrics.setNumberLeftCommits(commitDao.getCommitsByMsAndSide(msId, "left"));
			msMetrics.setNumberRightCommits(commitDao.getCommitsByMsAndSide(msId, "right"));

			msMetrics.setNumberDevelopers(devDao.getDevListByMs(msId).size());
			List<Integer> leftDevs = devDao.getNumberDevbyMsAndSide(msId, "left");
			msMetrics.setNumberLeftDevelopers(leftDevs.size());
			List<Integer> rightDevs = devDao.getNumberDevbyMsAndSide(msId, "right");
			msMetrics.setNumberRightDevelopers(rightDevs.size());
			msMetrics.setNumberBothSideDevelopers(getIntersection(leftDevs, rightDevs));
			
			msMetrics.setCodeChurn(computeCodeChurn(msDao.getListOfChunksFromOneMergeScenario(msId)));
			msMetrics.setConflictingCodeChurn(computeCodeChurn(msDao.getListOfConflictingChunksFromOneMergeScenario(msId)));

			msMetricsList.add(msMetrics);
		}

		DBWriter.INSTANCE.persistMsMetrics(msMetricsList);
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
	
	private Integer computeCodeChurn(List<Chunk<?>> listChunksFromOneMergeScenario) {
		int codeChurn = 0;
		for(Chunk<?> currentChunk : listChunksFromOneMergeScenario){
			int beginLine = currentChunk.getBeginLine();
			int endLine = currentChunk.getEndLine();
			int diff = 1 + endLine - beginLine;
			codeChurn = codeChurn + diff;
		}
		
		return codeChurn;
	}
}
