package commnet.builder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import commnet.model.beans.DeveloperRole;
import commnet.model.beans.Project;
import commnet.model.dao.DeveloperRoleDao;
import commnet.model.dao.MergeScenarioDao;
import commnet.model.db.DBWriter;
import commnet.model.exceptions.InvalidBeanException;

public class IntegratorBuilder {
	private Project project;

	private MergeScenarioDao msdao = new MergeScenarioDao();
	private DeveloperRoleDao drdao = new DeveloperRoleDao();

	public IntegratorBuilder(Project projectIdDB) {
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
			List<DeveloperRole> devToStore = new ArrayList<>();

			// get list merge scenario ids by project id
			List<DeveloperRole> devRoleList = msdao.getIntegratorListByProject(project.getIdDB());

			devToStore.addAll(devRoleList);
			
			// remove the ones that are already in the database (same
			// contributor and merge scenario)
			List<DeveloperRole> devInDBList = drdao.getDeveloperRoleListByProjectId(project.getIdDB(), "integrators");
			
			for (DeveloperRole devRoleInDB : devInDBList){
				for (DeveloperRole devRole : devRoleList){
					if (devRoleInDB.getContributorIdDB().equals(devRole.getContributorIdDB())
							&& devRoleInDB.getMergeScenarioIdDB().equals(devRole.getMergeScenarioIdDB())) {
						devToStore.remove(devRole);
					}
				}
			}
			
			// save committers into the database
			if(!devToStore.isEmpty()){
				DBWriter.INSTANCE.persistDeveloperRoleList(devToStore, "integrators");
			}

		} catch (InvalidBeanException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
