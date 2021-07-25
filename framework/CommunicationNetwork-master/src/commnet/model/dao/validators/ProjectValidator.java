package commnet.model.dao.validators;

import commnet.model.beans.Project;
import commnet.model.exceptions.InvalidBeanException;

public class ProjectValidator implements Validator<Project> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	@Override
	public boolean validate(Project p) throws InvalidBeanException {
		if (p == null || p.getName() == null || p.getUrl() == null) {
			throw new InvalidBeanException(Project.class,
					"Either the object itself, the `Name', or the `URL' are <null>.", new NullPointerException());
		}

		if (p.getName().equals("") || p.getUrl().equals("")) {
			throw new InvalidBeanException(Project.class,
					"Either the object itself, the `Name', or the `URL' are empty.", new IllegalArgumentException());
		}
		return true;
	}
}
