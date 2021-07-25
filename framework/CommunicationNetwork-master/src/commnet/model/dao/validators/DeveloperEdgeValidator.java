package commnet.model.dao.validators;

import java.sql.SQLException;

import commnet.model.beans.DeveloperEdge;
import commnet.model.dao.DAOFactory;
import commnet.model.dao.DAOFactory.Bean;
import commnet.model.dao.DeveloperNodeDao;
import commnet.model.exceptions.InvalidBeanException;

public class DeveloperEdgeValidator implements Validator<DeveloperEdge> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * commnet.model.dao.validators.Validator#validate(
	 * java.lang.Object)
	 */
	@Override
	public boolean validate(DeveloperEdge edge) throws InvalidBeanException, SQLException {
		DeveloperNodeDao dndao = (DeveloperNodeDao) DAOFactory.getDAO(Bean.NODE);
		if (edge == null || edge.getNetworkID() == null) {
			throw new InvalidBeanException(DeveloperEdge.class, "Either the object itself, the `NetworkID' are <null>.",
					new NullPointerException());
		}
		if (edge.getDevA().getIdDB() == null && edge.getDevB().getIdDB() != null) {
			if (edge.getDevA().getName().equals(edge.getDevB().getName())) {
				edge.setDevA(edge.getDevB());
			}
		} else if (edge.getDevB().getIdDB() == null && edge.getDevA().getIdDB() != null) {
			if (edge.getDevB().getName().equals(edge.getDevA().getName())) {
				edge.setDevB(edge.getDevA());
			}
			// next two if's set the ID in the database if the developer is in
			// the database, but have null IdDB in the edge
		}
		if (edge.getDevA().getIdDB() == null) {
			edge.getDevA().setIdDB(dndao.get(edge.getDevA()).getIdDB());
		}
		if (edge.getDevB().getIdDB() == null) {
			edge.getDevB().setIdDB(dndao.get(edge.getDevB()).getIdDB());
		}
		if (edge.getDevB().getIdDB() == null && edge.getDevA().getIdDB() == null) {
			throw new InvalidBeanException(DeveloperEdge.class,
					"Either the object itself, the LeftID or RightID are <null>.", new NullPointerException());
		}

		if (dndao.get(edge.getDevA()) == null || dndao.get(edge.getDevB()) == null) {
			throw new InvalidBeanException(DeveloperEdge.class,
					"Either the `DevA' or the `DevB' nodes are non-existent in the database.",
					new NullPointerException());
		}

		return true;
	}

}
