package commnet.model.dao.validators;

import java.sql.SQLException;

import commnet.model.beans.DeveloperNode;
import commnet.model.exceptions.InvalidBeanException;

public class DeveloperNodeValidator implements Validator<DeveloperNode> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	@Override
	public boolean validate(DeveloperNode node) throws InvalidBeanException, SQLException {
		if (node == null || node.getEmail() == null) {
			throw new InvalidBeanException(DeveloperNode.class,
					"Either the object itself, the `Email', or the `projectID' are <null>.",
					new NullPointerException());
		}

		return true;
	}
}
