package commnet.test;

import java.util.List;

import githubinterface.GitHubRepository;
import githubinterface.PullRequest;
import githubinterface.datadefinitions.State;
import gitwrapper.process.ToolNotWorkingException;
import gitwrapper.repo.GitWrapper;
import gitwrapper.repo.Repository;
import commnet.util.Directories;

public class testgetCommit {

	public static void main(String[] args) throws ToolNotWorkingException {
		GitWrapper wrapper = new GitWrapper("git");
		Repository r = wrapper
				.clone(Directories.getReposDir(), "https://github.com/...", false)
				.get();
		GitHubRepository repo = new GitHubRepository(r, wrapper);

		List<PullRequest> prs = repo.getPullRequests(State.ANY).get();

	}
}
