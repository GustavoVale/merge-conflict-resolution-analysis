package commnet.model.dao.validators;

import java.sql.SQLException;

import commnet.model.beans.DeveloperNode;
import commnet.model.exceptions.InvalidBeanException;

public class EmailValidator implements Validator<DeveloperNode> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	@Override
	public boolean validate(DeveloperNode dev) throws InvalidBeanException, SQLException {
		if (dev.getEmail().equals("")) {
			return false;
		} else {
			return true;
		}
	}

}
