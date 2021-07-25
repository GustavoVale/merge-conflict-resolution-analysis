package commnet.model.dao.validators;

import commnet.model.exceptions.InvalidBeanException;

import java.sql.SQLException;

public interface Validator<T> {

	/**
	 * Check the Object to see if it will not fail in the database
	 * 
	 * @param obj
	 * @return true if the Object is valid or throws to an Exception otherwise
	 * @throws InvalidBeanException
	 * @throws SQLException
	 */
	boolean validate(T obj) throws InvalidBeanException, SQLException;
}
