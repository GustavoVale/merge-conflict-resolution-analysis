package commnet.model.dao.validators;

import java.sql.SQLException;

import commnet.model.beans.Network;
import commnet.model.exceptions.InvalidBeanException;

public class NetworkValidator implements Validator<Network> {

	// For now it does nothing
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	public boolean validate(Network comm) throws InvalidBeanException, SQLException {

		return true;
	}
}
