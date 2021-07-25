package commnet.model.beans;

import java.util.ArrayList;
import java.util.List;

import gitwrapper.repo.Commit;
import gitwrapper.repo.Repository;

public class Project {

	private String name;
	private String url;
	private Repository repository;
	private Integer idDB;
	private List<Network> networkList;
	private List<Issue> issueList;
	private List<Commit> commitList;

	// DB use this constructor
	public Project() {
		this(null, null, null, null);
	}

	public Project(String anURL) {
		this(null, getRepositoryProjectName(anURL), anURL, null);
	}

	public Project(Integer id, String aName, String anURL) {
		this(id, getRepositoryProjectName(anURL), anURL, null);
	}

	public Project(Integer idDB, String aName, String anURL, Repository aRepository) {
		setIdDB(idDB);
		setName(aName);
		setUrl(anURL);
		setRepository(aRepository);
		setNetworkList(new ArrayList<>());
		setCommitList(new ArrayList<>());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getIdDB() {
		return this.idDB;
	}

	private static String getRepositoryProjectName(String remoteURL) {
		String[] path = remoteURL.split("/");
		return path[path.length - 1];
	}

	public List<Network> getNetworkList() {
		return networkList;
	}

	public void setNetworkList(List<Network> network) {
		this.networkList = network;
	}

	public List<Issue> getIssueList() {
		return issueList;
	}

	public void setIssueList(List<Issue> issue) {
		this.issueList = issue;
	}

	public List<Commit> getCommitList() {
		return commitList;
	}

	public void setCommitList(List<Commit> commitList) {
		this.commitList = commitList;
	}
}
