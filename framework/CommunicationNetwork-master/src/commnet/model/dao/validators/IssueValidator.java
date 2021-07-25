package commnet.model.dao.validators;

import commnet.model.beans.Issue;
import commnet.model.exceptions.InvalidBeanException;

public class IssueValidator implements Validator<Issue> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	public boolean validate(Issue issue) throws InvalidBeanException {
		return true;
	}

}
