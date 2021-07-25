package commnet.metricsExtractor;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.DeveloperEdge;
import commnet.model.beans.NetworkMetrics;
import commnet.model.beans.Project;
import commnet.model.dao.DeveloperEdgeDao;
import commnet.model.dao.NetworkDao;
import commnet.model.dao.NetworkMetricDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;
import commnet.util.Logger;

public class NetworkMetricsExtractor {

	private Project project;
	private List<DeveloperEdge> edgeListType1;
	private List<DeveloperEdge> edgeListType2;
	private List<DeveloperEdge> edgeListType3;
	private List<DeveloperEdge> edgeListType4;
	private List<DeveloperEdge> edgeListType5;
	private List<DeveloperEdge> edgeListType6;
	private List<DeveloperEdge> edgeListType13;
	private List<DeveloperEdge> edgeListType14;
	private List<DeveloperEdge> edgeListType15;
	private List<DeveloperEdge> edgeListType16;
	private List<DeveloperEdge> edgeListType17;

	private NetworkDao netDao = new NetworkDao();
	private DeveloperEdgeDao edgeDao = new DeveloperEdgeDao();
	private NetworkMetricDao netMetricDao = new NetworkMetricDao();

	protected File log;

	public NetworkMetricsExtractor(Project project, File log) {
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

			List<Integer> networkIdList = netDao.getNetworkIdListByProject(project.getIdDB());
			List<Integer> netIdDBList = netMetricDao.getNetMetricsNetworkIdListByProject(project.getIdDB());

			if (networkIdList.size() != netIdDBList.size()) {
				for (Integer networkId : netIdDBList) {
					networkIdList.remove(networkId);
				}

				generateCoverage(networkIdList);
			}
		} catch (SQLException | InvalidBeanException e1) {
			Logger.log(log, "ERROR COMPUTING NET METRICS.");
			e1.printStackTrace();
		}
	}

	private void generateCoverage(List<Integer> netMetricsIdList) {
		List<NetworkMetrics> netMetricsList = new ArrayList<>();
		try {

			for (Integer netId : netMetricsIdList) {
				edgeListType1 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 1);
				edgeListType2 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 2);
				edgeListType3 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 3);
				edgeListType4 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 4);
				edgeListType5 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 5);
				edgeListType6 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 6);
				edgeListType13 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 13);
				edgeListType14 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 14);
				edgeListType15 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 15);
				edgeListType16 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 16);
				edgeListType17 = edgeDao.getEdgeListByNetworkIdAndEdgeType(netId, 17);

				List<DeveloperEdge> chunkBasedNetwork = getUnionEdgeTypes(edgeListType1, edgeListType2);
				List<DeveloperEdge> fileBasedNetwork = getUnionEdgeTypes(edgeListType3, edgeListType4);
				List<DeveloperEdge> mergeBasedNetwork = getUnionEdgeTypes(edgeListType5, edgeListType6);
				List<DeveloperEdge> preciseNetwork = getUnionEdgeTypes(edgeListType13, edgeListType14);
				List<DeveloperEdge> comprehensiveNetwork = getUnionEdgeTypes(edgeListType15, edgeListType16);
				List<DeveloperEdge> changedArtifactNetwork = edgeListType17;

				fileBasedNetwork = getUnionEdgeTypes(fileBasedNetwork, chunkBasedNetwork);
				mergeBasedNetwork = getUnionEdgeTypes(mergeBasedNetwork, fileBasedNetwork);

				NetworkMetrics netCoverageMetrics = new NetworkMetrics(netId);

				netCoverageMetrics.setCovFileOverChunk(computeCoverage(fileBasedNetwork, chunkBasedNetwork));
				netCoverageMetrics.setCovMSOverFile(computeCoverage(mergeBasedNetwork, fileBasedNetwork));
				netCoverageMetrics.setCovChunkBasedOverPrecise(computeCoverage(chunkBasedNetwork, preciseNetwork));
				netCoverageMetrics
						.setCovChunkBasedOverComprehensive(computeCoverage(chunkBasedNetwork, comprehensiveNetwork));
				netCoverageMetrics
						.setCovChunkBasedOverArtifact(computeCoverage(chunkBasedNetwork, changedArtifactNetwork));
				netCoverageMetrics.setCovPreciseOverChunkBased(computeCoverage(preciseNetwork, chunkBasedNetwork));
				netCoverageMetrics
						.setCovComprehensiveOverChunkBased(computeCoverage(comprehensiveNetwork, chunkBasedNetwork));
				netCoverageMetrics
						.setCovArtifactOverChunkBased(computeCoverage(changedArtifactNetwork, chunkBasedNetwork));
				netCoverageMetrics.setCovFileBasedOverPrecise(computeCoverage(fileBasedNetwork, preciseNetwork));
				netCoverageMetrics
						.setCovFileBasedOverComprehensive(computeCoverage(fileBasedNetwork, comprehensiveNetwork));
				netCoverageMetrics
						.setCovFileBasedOverArtifact(computeCoverage(fileBasedNetwork, changedArtifactNetwork));
				netCoverageMetrics.setCovPreciseOverFileBased(computeCoverage(preciseNetwork, fileBasedNetwork));
				netCoverageMetrics
						.setCovComprehensiveOverFileBased(computeCoverage(comprehensiveNetwork, fileBasedNetwork));
				netCoverageMetrics
						.setCovArtifactOverFileBased(computeCoverage(changedArtifactNetwork, fileBasedNetwork));
				netCoverageMetrics.setCovMSBasedOverPrecise(computeCoverage(mergeBasedNetwork, preciseNetwork));
				netCoverageMetrics
						.setCovMSBasedOverComprehensive(computeCoverage(mergeBasedNetwork, comprehensiveNetwork));
				netCoverageMetrics
						.setCovMSBasedOverArtifact(computeCoverage(mergeBasedNetwork, changedArtifactNetwork));
				netCoverageMetrics.setCovPreciseOverMSBased(computeCoverage(preciseNetwork, mergeBasedNetwork));
				netCoverageMetrics
						.setCovComprehensiveOverMSBased(computeCoverage(comprehensiveNetwork, mergeBasedNetwork));
				netCoverageMetrics
						.setCovArtifactOverMSBased(computeCoverage(changedArtifactNetwork, mergeBasedNetwork));
				netCoverageMetrics.setNumberComprehensiveEdges(comprehensiveNetwork.size());
				netCoverageMetrics.setNumberPreciseEdges(preciseNetwork.size());
				netCoverageMetrics.setNumberArtifactEdges(changedArtifactNetwork.size());

				netCoverageMetrics.setNumberChunkEdges(chunkBasedNetwork.size());
				netCoverageMetrics.setNumberFileEdges(fileBasedNetwork.size());
				netCoverageMetrics.setNumberMSEdges(mergeBasedNetwork.size());

				netMetricsList.add(netCoverageMetrics);

			}

			DBWriter.INSTANCE.persistNetMetrics(netMetricsList);

		} catch (InvalidBeanException | SQLException e) {
			Logger.log(log, "ERROR COMPUTING REMAINGING NET METRICS.");
			e.printStackTrace();
		}
	}

	private List<DeveloperEdge> getUnionEdgeTypes(List<DeveloperEdge> list1, List<DeveloperEdge> list2) {
		List<DeveloperEdge> result = new ArrayList<>();

		for (DeveloperEdge edge1 : list1) {
			boolean itHas = false;
			for (DeveloperEdge edge2 : list2) {
				if (edge1.devsEqualIDs(edge2)) {
					itHas = true;
					break;
				}

			}
			if (!itHas && !edge1.getDevA().equalIds(edge1.getDevB())) {
				result.add(edge1);
			}
		}

		for (DeveloperEdge edge : list2) {
			if (!edge.getDevA().equalIds(edge.getDevB())) {
				result.add(edge);
			}
		}

		return result;
	}

	private Integer computeCoverage(List<DeveloperEdge> net1, List<DeveloperEdge> net2) {

		float intersectionNet1AndNet2 = getEdgeIntersection(net1, net2).size();
		float sizeNet1 = net1.size();
		Integer coverage = 0;

		if (sizeNet1 != 0) {
			coverage = (int) ((intersectionNet1AndNet2 / sizeNet1) * 100);
		}
		return coverage;
	}

	private List<DeveloperEdge> getEdgeIntersection(List<DeveloperEdge> net1, List<DeveloperEdge> net2) {
		List<DeveloperEdge> result = new ArrayList<>();

		if (net1.size() != 0 && net2.size() != 0) {
			for (DeveloperEdge edge1 : net1) {
				for (DeveloperEdge edge2 : net2) {
					if (edge1.devsEqualIDs(edge2)) {
						result.add(edge1);
						break;
					}
				}
			}
		}
		return result;
	}

}
