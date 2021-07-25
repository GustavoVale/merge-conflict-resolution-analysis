package commnet.metricsExtractor;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gitwrapper.repo.BlameLine;
import gitwrapper.repo.GitWrapper;
import commnet.model.beans.CommitN;
import commnet.model.beans.MergeConflictInfo;
import commnet.model.beans.MergeConflictMetrics;
import commnet.model.beans.Project;
import commnet.model.dao.CommitDao;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.MergeConflictInfoDao;
import commnet.model.dao.MergeConflictMetricsDao;
import commnet.model.db.DBWriter;
import commnet.model.enums.ChangeType;
import commnet.model.exceptions.InvalidBeanException;

public class MergeConflictMetricsExtractor {

	private Project project;
	private GitWrapper git;

	private MergeConflictMetricsDao mcmDao = new MergeConflictMetricsDao();
	private CommitDao cDao = new CommitDao();
	private DeveloperNodeDao devNodeDao = new DeveloperNodeDao();
	private MergeConflictInfoDao mciDao = new MergeConflictInfoDao();

	private List<String> listOfIntegratorEmails = new ArrayList<>();
	private HashMap<Integer, Integer> mapMergeScenarioIdAndMergeCommitId = new HashMap<>();

	private CommitN auxMergeCommit = new CommitN(0);
	private String auxFilePath = "";
	private boolean auxDevHasKnowledge = false;
	private HashMap<Integer, String> mapMergeConflictInfoIdAndFilePath = new HashMap<>();

	private HashMap<Integer, ArrayList<MergeConflictMetrics>> mapMergeScenarioAndListMergeConflictMetrics = new HashMap<>();
	private List<MergeConflictMetrics> newMergeConflictMetricsList = new ArrayList<>();
	private List<MergeConflictMetrics> existingMergeConflictMetricsInstancesList = new ArrayList<>();

	public MergeConflictMetricsExtractor(Project project) {
		setProject(project);
		git = getProject().getRepository().getGit();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void storer() {
		try {

			// Get a map of merge scenarios and a list of mergeConflictMetrics for a target
			// project
			mapMergeScenarioAndListMergeConflictMetrics = mcmDao.getListMergeConflictMetrics(getProject().getIdDB());

			// Retrieving the merge commit of each merge scenario.
			// It also adds instances in a list to retrieve merge scenario ids that have
			// merge conflict metrics saved into the database
			for (Map.Entry<Integer, ArrayList<MergeConflictMetrics>> mergeScenario : mapMergeScenarioAndListMergeConflictMetrics
					.entrySet()) {

				List<MergeConflictMetrics> listMergeConflictMetricsOfMergeScenario = mergeScenario.getValue();
				MergeConflictMetrics aux = listMergeConflictMetricsOfMergeScenario.get(0);
				auxMergeCommit = cDao.getByIdDB(aux.getMergeCommitIdDB());
				for (MergeConflictMetrics mergeConflictMetrics : listMergeConflictMetricsOfMergeScenario) {
					mergeConflictMetrics.setMergeCommit(auxMergeCommit);
				}
			}

			// Get all merge scenario ids and merge commits of conflicting merge scenarios.
			// Merge conflicts are required to check integrators' e-mails
			mapMergeScenarioIdAndMergeCommitId = cDao
					.getMapOfMergeScenarioIDsAndMergeCommitsIDsOfConflictingMergeScenariosPerProject(
							getProject().getIdDB());

			// Loop for each conflicting merge scenario
			for (Map.Entry<Integer, Integer> conflictingMergeScenario : mapMergeScenarioIdAndMergeCommitId.entrySet()) {

				// Get a list of e-mails linked with each integrator of each merge scenario
				listOfIntegratorEmails = devNodeDao.getContEmailListByCommitId(conflictingMergeScenario.getValue());

				// Checking conflicting merge scenarios that have some merge_conflict_metrics
				// instance stored
				if (mapMergeScenarioAndListMergeConflictMetrics.containsKey(conflictingMergeScenario.getKey())) {

					ArrayList<MergeConflictMetrics> listOfMergeConflictMetrics = mapMergeScenarioAndListMergeConflictMetrics
							.get(conflictingMergeScenario.getKey());

					Map<Integer, MergeConflictInfo> mergeConflictMapOfMergeScenario = mciDao
							.getMciByMsId(conflictingMergeScenario.getKey());

					getInformationWhetherIntegratorHadKnowledgeOnMergeScenarioAndTypeOfChange(
							listOfMergeConflictMetrics, mergeConflictMapOfMergeScenario);

					// if there is no merge_conflict_metric for a conflicting merge scenario, we
					// create for the ones that have already merge_conflict_info
				} else {

					mapMergeConflictInfoIdAndFilePath = mciDao
							.getListMergeConflictInfoIdsByMergeScenario(conflictingMergeScenario.getKey());

					auxMergeCommit = cDao.getByIdDB(conflictingMergeScenario.getValue());

					List<MergeConflictMetrics> newMergeConflictMetricsInstancesList = new ArrayList<>();

					for (Map.Entry<Integer, String> mergeConflictInfo : mapMergeConflictInfoIdAndFilePath.entrySet()) {

						newMergeConflictMetricsInstancesList
								.add(new MergeConflictMetrics(null, mergeConflictInfo.getKey(), ChangeType.OTHER, 0, 0,
										0, 0, 0, 0, false, getProject().getIdDB(), conflictingMergeScenario.getKey(),
										auxMergeCommit.getIdDB(), mergeConflictInfo.getValue(), auxMergeCommit));
					}

					if (!newMergeConflictMetricsInstancesList.isEmpty()) {


						Map<Integer, MergeConflictInfo> mergeConflictMapOfMergeScenario = mciDao
								.getMciByMsId(conflictingMergeScenario.getKey());

						getInformationWhetherIntegratorHadKnowledgeOnMergeScenarioAndTypeOfChange(
								newMergeConflictMetricsInstancesList, mergeConflictMapOfMergeScenario);

						newMergeConflictMetricsList.addAll(newMergeConflictMetricsInstancesList);
					}
				}
			}

			// Update a list merge_conflict_metrics
			if (!mapMergeScenarioAndListMergeConflictMetrics.isEmpty()) {

				for (Map.Entry<Integer, ArrayList<MergeConflictMetrics>> mcmInstance : mapMergeScenarioAndListMergeConflictMetrics
						.entrySet()) {
					existingMergeConflictMetricsInstancesList.addAll(mcmInstance.getValue());
				}

				DBWriter.INSTANCE.persistMergeConflictMetrics(existingMergeConflictMetricsInstancesList, false);
			}

			// Save a list of new merge_conflict_metrics
			if (!newMergeConflictMetricsList.isEmpty()) {
				DBWriter.INSTANCE.persistMergeConflictMetrics(newMergeConflictMetricsList, true);

			}

		} catch (InvalidBeanException | SQLException e1) {
			e1.printStackTrace();
		}
	}

	private void getInformationWhetherIntegratorHadKnowledgeOnMergeScenarioAndTypeOfChange(
			List<MergeConflictMetrics> listOfMergeConflictMetrics,
			Map<Integer, MergeConflictInfo> mergeConflictMapOfMergeScenario) {

		git.exec(getProject().getRepository().getDir(), listOfMergeConflictMetrics.get(0).getMergeCommit().getHash());

		for (MergeConflictMetrics mcm : listOfMergeConflictMetrics) {

			if (!auxDevHasKnowledge) {

				// Different file then we need to blame file
				if (auxFilePath != mcm.getFilePath()) {
					comparingAuthorEmailsFromFileAndMergeCommit(mcm);
				}

			} else {
				mcm.setDevHasKnowledge(auxDevHasKnowledge);
			}

			mcm.setChangeType(getMergeConflictInfoChangeType(
					mergeConflictMapOfMergeScenario.get(mcm.getMergeConflictInfoIdDB())));

		}
		auxDevHasKnowledge = false;

	}

	private void comparingAuthorEmailsFromFileAndMergeCommit(MergeConflictMetrics mcm) {
		Set<String> blameLinesAuthorEmail = new HashSet<String>();

		// blame
		Optional<List<BlameLine>> fileLines = getProject().getRepository().blameFile(Paths.get(mcm.getFilePath()));
		auxFilePath = mcm.getFilePath();

		if (fileLines.isPresent()) {

			// Get set of author commit e-mails of determined file
			for (BlameLine line : fileLines.get()) {
				blameLinesAuthorEmail.add(line.commit.getAuthorMail());
			}

			for (String authorEmail : blameLinesAuthorEmail) {
				if (listOfIntegratorEmails.contains(authorEmail)) {
					mcm.setDevHasKnowledge(true);
					auxDevHasKnowledge = true;
					break;
				} else {
					auxDevHasKnowledge = false;
				}
			}
		}

	}

	private ChangeType getMergeConflictInfoChangeType(MergeConflictInfo currentMergeConflictInfo) {

		ChangeType changeType;

		String left = cleanningFormatingChanges(currentMergeConflictInfo.getLeftCommitCode());
		String right = cleanningFormatingChanges(currentMergeConflictInfo.getRightCommitCode());

		if (left.equalsIgnoreCase(right)) {
			changeType = ChangeType.FORMATTING;
		} else {

			left = cleanningDeclarationChanges(left);
			right = cleanningDeclarationChanges(right);

			if (left.equalsIgnoreCase(right)) {
				changeType = ChangeType.ACCESS_MODIFIER;
			} else {
				changeType = ChangeType.OTHER;
			}
		}

		return changeType;
	}

	private String cleanningFormatingChanges(String string) {

		string = string.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\t", "").replaceAll("\r", "");
		return string;
	}

	private String cleanningDeclarationChanges(String string) {

		string = string.replaceAll("public", "").replaceAll("protected", "").replaceAll("private", "");
		return string;
	}

}
