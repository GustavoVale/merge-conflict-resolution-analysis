package commnet.model.dao;

public abstract class DAOFactory {

	public enum Bean {
		PROJECT, COMMIT, NETWORK, EDGE, NODE, EMAIL, ISSUE, LABEL
	}

	public static DAO<?> getDAO(Bean type) {
		switch (type) {
		case PROJECT:
			return (DAO<?>) new ProjectDao();
		case COMMIT:
			return new CommitDao();
		case NETWORK:
			return new NetworkDao();
		case EDGE:
			return new DeveloperEdgeDao();
		case NODE:
			return new DeveloperNodeDao();
		case EMAIL:
			return new EmailDao();
		case ISSUE:
			return new IssueDao();
		case LABEL:
			return new LabelDao();
		}
		return null;
	}
}
