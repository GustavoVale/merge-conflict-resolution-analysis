package commnet.builder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.DeveloperRole;
import commnet.model.beans.Project;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.dao.DeveloperRoleDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;

public class CommitterBuilder {
	private Project project;

	private DeveloperNodeDao ddao = new DeveloperNodeDao();
	private MergeScenarioDao msdao = new MergeScenarioDao();
	private DeveloperRoleDao drdao = new DeveloperRoleDao();

	public CommitterBuilder(Project projectIdDB) {
		setProject(projectIdDB);
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project projectIdDB) {
		this.project = projectIdDB;
	}

	public void storer() {
		try {

			List<DeveloperRole> devRoleList = new ArrayList<>();
			List<DeveloperRole> devRoleToStoreList = new ArrayList<>();

			// get list merge scenario ids by project id
			List<Integer> mergeScenarioIdList = msdao.getMSListByProject(project.getIdDB());

			// for each merge scenario get the committers into the database
			for (Integer mergeScenarioId : mergeScenarioIdList) {

				List<Integer> committerByMsIdList = ddao.getCommitterListByMs(mergeScenarioId);
				for (Integer devRoleID : committerByMsIdList){
					devRoleList.add(new DeveloperRole(null, mergeScenarioId, devRoleID));
				}
			}

			devRoleToStoreList.addAll(devRoleList);
			
			// remove the ones that are already in the database (same
			// contributor and merge scenario)
			List<DeveloperRole> devInDBList = drdao.getDeveloperRoleListByProjectId(project.getIdDB(), "committers");
			for (DeveloperRole devRoleInDB : devInDBList){
				for (DeveloperRole devRole : devRoleList){
					if (devRoleInDB.getContributorIdDB().equals(devRole.getContributorIdDB())
							&& devRoleInDB.getMergeScenarioIdDB().equals(devRole.getMergeScenarioIdDB())) {
						devRoleToStoreList.remove(devRole);
					}
				}
			}
			
			// save committers into the database
			if(!devRoleList.isEmpty()){
				DBWriter.INSTANCE.persistDeveloperRoleList(devRoleToStoreList, "committers");
			}

		} catch (InvalidBeanException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
