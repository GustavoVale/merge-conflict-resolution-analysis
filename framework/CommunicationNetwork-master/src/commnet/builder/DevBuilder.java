package commnet.builder;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gitwrapper.process.ProcessExecutor.ExecRes;
import gitwrapper.repo.BlameLine;
import gitwrapper.repo.GitWrapper;
import commnet.model.beans.Developer;
import commnet.model.beans.DeveloperNode;
import commnet.model.beans.DeveloperRole;
import commnet.model.beans.Project;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.DeveloperRoleDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;

public class DevBuilder {

	private Project project;

	private GitWrapper git;
	private MergeScenarioBuilder msBuilder;

	private DeveloperNodeDao ddao = new DeveloperNodeDao();
	private MergeScenarioDao msdao = new MergeScenarioDao();
	private DeveloperRoleDao drdao = new DeveloperRoleDao();

	public DevBuilder(Project projectIdDB) {
		setProject(projectIdDB);
		git = getProject().getRepository().getGit();
		msBuilder = new MergeScenarioBuilder(projectIdDB, true, null);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project projectIdDB) {
		this.project = projectIdDB;
	}

	public void storer() {
		try {

			List<Developer> devsList = new ArrayList<>();
			List<Developer> devToStore = new ArrayList<>();

			// get list merge scenario ids by project id
			List<Integer> mergeScenarioIdList = msdao.getMSListByProject(project.getIdDB());

			// for each merge scenario get the developers that contribute to the project
			for (Integer mergeScenarioId : mergeScenarioIdList) {
				devToStore.addAll(getDevelopersList(mergeScenarioId));
			}

			devsList.addAll(devToStore);
			// remove the ones that are already in the database (same contributor and merge
			// scenario)
			List<DeveloperRole> devInDBList = drdao.getDeveloperRoleListByProjectId(project.getIdDB(), "devs");
			for (DeveloperRole devRole : devInDBList) {
				for (Developer dev : devsList) {
					if (dev.getContributorIdDB().equals(devRole.getContributorIdDB())
							&& dev.getMergeScenarioIdDB().equals(devRole.getMergeScenarioIdDB())) {
						devToStore.remove(dev);
					}
				}
			}

			// save devs into the database
			if (!devToStore.isEmpty()) {
				DBWriter.INSTANCE.persistDeveloperList(devToStore);
			}

		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create a list of developers *devs* that contribute to the merge scenario
	 * 
	 * @param mergeScenarioId
	 * @return a list of devs
	 */
	private List<Developer> getDevelopersList(Integer mergeScenarioId) {

		HashMap<DeveloperNode, Integer> devMapAndLinesOfCodeChanged = new HashMap<>();

		try {
			String mergeCommitHash = msdao.getMergeCommitHash(mergeScenarioId);
			git.exec(getProject().getRepository().getDir(), "checkout", mergeCommitHash);
		} catch (InvalidBeanException | SQLException e3) {
			e3.printStackTrace();
		}

		// LoC to get core developers
		devMapAndLinesOfCodeChanged = blameAllFilesPresentInTheRepository();

		devMapAndLinesOfCodeChanged = removeDuplicatedDeveloperNodes(devMapAndLinesOfCodeChanged);

		devMapAndLinesOfCodeChanged = addingDevsNotPresentInRepo(devMapAndLinesOfCodeChanged, mergeScenarioId);

		// Order the Map by the values (numberOfLOCByDeveloper)
		Map<DeveloperNode, Integer> devStringMapSorted = devMapAndLinesOfCodeChanged.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

		ArrayList<Developer> developerList = creatingDevelopersByMergeScenarios(devStringMapSorted, mergeScenarioId);

		return developerList;
	}

	/**
	 * Blame each file present in the repository and returns a map with of
	 * contributors and number of lines the added (own)
	 * 
	 * @return map key:contributors value:LoC added
	 */
	private HashMap<DeveloperNode, Integer> blameAllFilesPresentInTheRepository() {
		HashMap<DeveloperNode, Integer> devMapAndLinesOfCodeChanged = new HashMap<>();

		// Get all files in the repository
		Optional<ExecRes> repositoryFiles = git.exec(getProject().getRepository().getDir(), "ls-files");

		if (repositoryFiles.isPresent()) {
			String[] filePathArray = repositoryFiles.get().stdOut.toString().split("\\n+");

			// Remove duplicated paths
			Set<String> uniqueFilePathSet = new HashSet<String>(Arrays.asList(filePathArray));

			// Iterate in each file to get developers and the number of files they changed
			for (String filePath : uniqueFilePathSet) {
				Path newPath = Paths.get(filePath);
				try {

					// Ignore the binary one since we cannot blame them
					if (!msBuilder.isBinary(newPath)) {
						Optional<List<BlameLine>> blameLineList = getProject().getRepository().blameFile(newPath);
						if (blameLineList.isPresent()) {

							// Iterate in each line of the file
							for (BlameLine line : blameLineList.get()) {

								DeveloperNode contributor = new DeveloperNode(null, line.commit.getAuthor(),
										line.commit.getAuthorMail());
								if (!devMapAndLinesOfCodeChanged.containsKey(contributor)) {
									devMapAndLinesOfCodeChanged.put(contributor, 1);
								} else {
									Integer currentvalue = devMapAndLinesOfCodeChanged.get(contributor);
									devMapAndLinesOfCodeChanged.replace(contributor, currentvalue + 1);
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return devMapAndLinesOfCodeChanged;
	}

	/**
	 * Get a map and remove duplicate keys after check in the database
	 * 
	 * @param devMapAndLinesOfCodeChanged
	 * @return map key:contributors value:LoC added
	 */
	private HashMap<DeveloperNode, Integer> removeDuplicatedDeveloperNodes(
			HashMap<DeveloperNode, Integer> devMapAndLinesOfCodeChanged) {
		HashMap<DeveloperNode, Integer> devMapUpdatedAndLinesOfCodeChanged = new HashMap<>();

		for (Map.Entry<DeveloperNode, Integer> entry : devMapAndLinesOfCodeChanged.entrySet()) {
			DeveloperNode key = entry.getKey();
			Integer value = entry.getValue();

			try {
				DeveloperNode contributor = ddao.get(key);

				// case we don't catch contributor by name, we try via email and, if she is not
				// in the database, we save her
				if (contributor == null) {
					contributor = DBWriter.INSTANCE.persistNode(key);
					contributor = ddao.get(contributor);
				}

				if (!devMapUpdatedAndLinesOfCodeChanged.containsKey(contributor)) {
					devMapUpdatedAndLinesOfCodeChanged.put(contributor, value);
				} else {
					Integer currentvalue = devMapUpdatedAndLinesOfCodeChanged.get(contributor);
					devMapUpdatedAndLinesOfCodeChanged.replace(contributor, currentvalue + value);
				}
			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
			}
		}
		return devMapUpdatedAndLinesOfCodeChanged;
	}

	/**
	 * Add devs that contribute to the merge scenario, but does not had their code
	 * accepted in the repository
	 * 
	 * @param devMapAndLinesOfCodeChanged
	 * @return Updated map with these devs key:devs value:LoC added
	 */
	private HashMap<DeveloperNode, Integer> addingDevsNotPresentInRepo(
			HashMap<DeveloperNode, Integer> devMapAndLinesOfCodeChanged, Integer msId) {

		try {
			HashMap<DeveloperNode, Integer> missedContributorsMap = new HashMap<>();

			List<DeveloperNode> contributorsIdList = ddao.getDeveloperNodeListByMs(msId);

			for (DeveloperNode cont : contributorsIdList) {
				if (!devMapAndLinesOfCodeChanged.keySet().contains(cont)) {
					missedContributorsMap.put(cont, 0);
				}
			}

			devMapAndLinesOfCodeChanged.putAll(missedContributorsMap);

		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
		}
		return devMapAndLinesOfCodeChanged;
	}

	/**
	 * Create a list of developers *devs* that contribute to the merge scenario (it
	 * includes all contributors)
	 * 
	 * @param devStringMapSorted
	 * @param mergeScenarioId
	 * @return ArrayList of devs
	 */
	private ArrayList<Developer> creatingDevelopersByMergeScenarios(Map<DeveloperNode, Integer> devStringMapSorted,
			Integer mergeScenarioId) {

		Developer leftBranchLeader = null;
		Developer rightBranchLeader = null;
		ArrayList<Developer> developerList = new ArrayList<>();
		boolean isCoreDev = true;
		boolean isIntegratorPresent = false;

		Developer lastCommitLeft = null;
		Developer lastCommitRight = null;

		// Get the number of lines in the repository
		Integer sumLinesOfCode = sumOfLinesOfCodeChangedInTheRepository(devStringMapSorted.values());

		// Setting a threshold for core developers (i.e., the ones that owns 80% of the
		// code)
		Integer threshold = (sumLinesOfCode * 20) / 100;

		Integer integratorIdDB = getIntegratorId(mergeScenarioId);

		// Iterate over map to set the metrics
		for (Map.Entry<DeveloperNode, Integer> entry : devStringMapSorted.entrySet()) {

			// create a new developer
			Developer newDev = new Developer(null, mergeScenarioId, entry.getKey().getIdDB(), isCoreDev);

			// set some values for the developer case we find her
			try {
				newDev = ddao.setIntegerMetricsToDeveloper(newDev, "left");
				newDev = ddao.setIntegerMetricsToDeveloper(newDev, "right");
			} catch (InvalidBeanException | SQLException e) {
				e.printStackTrace();
			}

			// Get leftBranchLeader
			if (leftBranchLeader == null && newDev.getNumberOfChunksChangedLeft() > 0) {
				leftBranchLeader = newDev;
			} else {
				if (leftBranchLeader != null
						&& leftBranchLeader.getNumberOfChunksChangedLeft() < newDev.getNumberOfChunksChangedLeft()) {
					leftBranchLeader = newDev;
				}
			}

			// Get rightBranchLeader
			if (rightBranchLeader == null && newDev.getNumberOfChunksChangedRight() > 0) {
				rightBranchLeader = newDev;
			} else {
				if (rightBranchLeader != null
						&& rightBranchLeader.getNumberOfChunksChangedRight() < newDev.getNumberOfChunksChangedRight()) {
					rightBranchLeader = newDev;
				}
			}

			// Get last dev to commit on the left side
			if (lastCommitLeft == null && newDev.getLastCommitLeftDate() != null) {
				lastCommitLeft = newDev;
			} else {
				if (newDev.getLastCommitLeftDate() != null
						&& lastCommitLeft.getLastCommitLeftDate().before(newDev.getLastCommitLeftDate())) {
					lastCommitLeft = newDev;
				}
			}

			// Get last dev to commit on the right side
			if (lastCommitRight == null && newDev.getLastCommitRightDate() != null) {
				lastCommitRight = newDev;
			} else {
				if (newDev.getLastCommitRightDate() != null
						&& lastCommitRight.getLastCommitRightDate().before(newDev.getLastCommitRightDate())) {
					lastCommitRight = newDev;
				}
			}

			if (newDev.getContributorIdDB().equals(integratorIdDB)) {
				newDev.setIntegrator(true);
				isIntegratorPresent = true;
			}

			developerList.add(newDev);
			sumLinesOfCode = sumLinesOfCode - entry.getValue();

			if (isCoreDev && sumLinesOfCode <= threshold) {
				isCoreDev = false;
			}
		}

		// Ensures that each merge scenario has an integrator
		if (!isIntegratorPresent) {
			developerList.add(new Developer(null, mergeScenarioId, integratorIdDB, 0, 0, 0, 0, 0, 0, 0, 0, false, false,
					true, false, false, false, false));
		}

		if (leftBranchLeader != null) {
			leftBranchLeader.setLeftBranchLeader(true);
		}
		if (rightBranchLeader != null) {
			rightBranchLeader.setRightBranchLeader(true);
		}
		if (lastCommitLeft != null) {
			lastCommitLeft.setLastChangeLeft(true);
		}
		if (lastCommitRight != null) {
			lastCommitRight.setLastChangeRight(true);
		}

		return developerList;
	}

	/**
	 * Get the Loc added to the project by all contributors
	 * 
	 * @param values
	 * @return LoC added to the repository
	 */
	private Integer sumOfLinesOfCodeChangedInTheRepository(Collection<Integer> values) {
		Integer sumLinesOfCode = 0;
		for (Integer devValue : values) {
			sumLinesOfCode = sumLinesOfCode + devValue;
		}
		return sumLinesOfCode;
	}

	/**
	 * Get integrator id
	 * 
	 * @param mergeScenarioId
	 * @return integrator (contributors) idDB
	 */
	private Integer getIntegratorId(Integer mergeScenarioId) {

		Integer integratorIdDB = 0;
		try {

			integratorIdDB = msdao.getIntegratorIdDBByMergeScenario(mergeScenarioId);

		} catch (InvalidBeanException | SQLException e) {
			e.printStackTrace();
		}

		return integratorIdDB;
	}

}
