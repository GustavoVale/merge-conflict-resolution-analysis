package commnet.model.dao.validators;

import commnet.model.beans.MergeScenario;
import commnet.model.exceptions.InvalidBeanException;

public class MergeScenarioValidator implements Validator<MergeScenario> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	public boolean validate(MergeScenario merge) throws InvalidBeanException {
		if (merge == null || merge.getMergeCommit() == null || merge.getBaseCommit() == null) {
			throw new InvalidBeanException(MergeScenario.class,
					"Either the object itself, the `ID', or the `Commit' are <null>.", new NullPointerException());
		}
		return true;
	}
}
