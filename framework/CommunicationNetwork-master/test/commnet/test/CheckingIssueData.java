package commnet.test;

	import java.util.ArrayList;
	import java.util.List;
	import java.util.Optional;

	import githubinterface.GitHubRepository;
	import githubinterface.PullRequest;
	import githubinterface.datadefinitions.IssueData;
	import githubinterface.datadefinitions.State;
	import gitwrapper.process.ToolNotWorkingException;
	import gitwrapper.repo.GitWrapper;
	import gitwrapper.repo.Repository;
	import commnet.util.Directories;

	public class CheckingIssueData {

		protected static final githubinterface.datadefinitions.State ANY = State.ANY;
		private  static List<PullRequest> prs1 = new ArrayList<>();
		private List<PullRequest> prs2 = new ArrayList<>();
		private List<PullRequest> prs3 = new ArrayList<>();
		private List<PullRequest> prs4 = new ArrayList<>();
		private List<PullRequest> prs5 = new ArrayList<>();
		private List<PullRequest> prs6 = new ArrayList<>();
		private List<PullRequest> prs7 = new ArrayList<>();
		private List<PullRequest> prs8 = new ArrayList<>();
		
		private List<IssueData> issuedata1 = new ArrayList<>();
		private List<IssueData> issuedata2 = new ArrayList<>();
		private List<IssueData> issuedata3 = new ArrayList<>();
		private List<IssueData> issuedata4 = new ArrayList<>();
		private List<IssueData> issuedata5 = new ArrayList<>();
		private List<IssueData> issuedata6 = new ArrayList<>();
		private List<IssueData> issuedata7 = new ArrayList<>();
		private List<IssueData> issuedata8 = new ArrayList<>();

		public static void main(String[] args) throws ToolNotWorkingException {
			
			GitWrapper wrapper1 = new GitWrapper("git");
			Repository r1 = wrapper1.clone(Directories.getReposDir(), "https://github.com/...", false).get();
			GitHubRepository repo1 = new GitHubRepository(r1, wrapper1, "05ac252994f55d1b4efbda7cf41dd2a2677f9ffd");
			
			Thread p1 = new Thread(repo1.getName()){
				public void run(){
					List<PullRequest> prs1 = repo1.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues1 = repo1.getIssues(true);
					setPrs1(prs1);
				}
			};
			
			GitWrapper wrapper2 = new GitWrapper("git");
			Repository r2 = wrapper2.clone(Directories.getReposDir(), "https://github.com/...", false).get();
			GitHubRepository repo2 = new GitHubRepository(r2, wrapper2, "f9aab5e5e9ad0192737569363d52350a25b83e12");
			
			Thread p2 = new Thread(repo2.getName()){
				public void run(){
					List<PullRequest> prs2 = repo2.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues2 = repo2.getIssues(true);
				}
			};
			
			GitWrapper wrapper3 = new GitWrapper("git");
			Repository r3 = wrapper3.clone(Directories.getReposDir(), "https://github.com/...", false).get();
			GitHubRepository repo3 = new GitHubRepository(r3, wrapper3, "6c7e385fb50db8a877d8f322cfcc89e71b760270");
			
			Thread p3 = new Thread(repo3.getName()){
				public void run(){
					List<PullRequest> prs3 = repo3.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues3 = repo3.getIssues(true);
				}
			};
			
			GitWrapper wrapper4 = new GitWrapper("git");
			Repository r4 = wrapper4.clone(Directories.getReposDir(), "https://github.com/...", false).get();
			GitHubRepository repo4 = new GitHubRepository(r4, wrapper4, "e273036f90b9fabbe4acad8e69045f3286ec90e0");
			
			Thread p4 = new Thread(repo4.getName()){
				public void run(){
					List<PullRequest> prs4 = repo4.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues4 = repo4.getIssues(true);
				}
			};
			
			GitWrapper wrapper5 = new GitWrapper("git");
			Repository r5 = wrapper5.clone(Directories.getReposDir(), "https://github.com/", false).get();
			GitHubRepository repo5 = new GitHubRepository(r5, wrapper5, "d89e3e476011edaf4253e95bc1fbb24862ebe386");
			
			
			Thread p5 = new Thread(repo5.getName()){
				public void run(){
					List<PullRequest> prs5 = repo5.getPullRequests(ANY).get();
					CheckingIssueData.setPrs1(prs5);
					Optional<List<IssueData>> issues5 = repo5.getIssues(true);
					System.out.println(issues5.get().toString());
				}
			};
			
			GitWrapper wrapper6 = new GitWrapper("git");
			Repository r6 = wrapper6.clone(Directories.getReposDir(), "https://github.com/", false).get();
			GitHubRepository repo6 = new GitHubRepository(r6, wrapper6, "ebd393317392e7c820919c58c3507922a7e04200");
			
			Thread p6 = new Thread(repo6.getName()){
				public void run(){
					List<PullRequest> prs6 = repo6.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues6 = repo6.getIssues(true);
				}
			};
			
			GitWrapper wrapper7 = new GitWrapper("git");
			Repository r7 = wrapper7.clone(Directories.getReposDir(), "https://github.com/", false).get();
			GitHubRepository repo7 = new GitHubRepository(r7, wrapper7, "3121568e074e1c61b5ff831c22a8731bd0018370");
			
			Thread p7 = new Thread(repo7.getName()){
				public void run(){
					List<PullRequest> prs7 = repo7.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues7 = repo7.getIssues(true);
				}
			};
			
			GitWrapper wrapper8 = new GitWrapper("git");
			Repository r8 = wrapper8.clone(Directories.getReposDir(), "https://github.com/", false).get();
			GitHubRepository repo8 = new GitHubRepository(r8, wrapper8, "80448e798b9fea3cece53b1db6c2ee688a331aa3");
			
			Thread p8 = new Thread(repo8.getName()){
				public void run(){
					List<PullRequest> prs8 = repo8.getPullRequests(ANY).get();
					Optional<List<IssueData>> issues8 = repo1.getIssues(true);
				}
			};
			
			p1.start();
			p2.start();
			p3.start();
			p4.start();
			p5.start();
			p6.start();
			p7.start();
			p8.start();
			
			try{
				p1.join();
				p2.join();
				p3.join();
				p4.join();
				p5.join();
				p6.join();
				p7.join();
				p8.join();
				
			}catch (InterruptedException e){
				Error er = new Error(e);
				throw er;
			}
			System.out.println("Finished");
		}

		public List<PullRequest> getPrs1() {
			return prs1;
		}

		public static void setPrs1(List<PullRequest> prs1) {
			CheckingIssueData.prs1 = prs1;
		}

		public List<PullRequest> getPrs2() {
			return prs2;
		}

		public void setPrs2(List<PullRequest> prs2) {
			this.prs2 = prs2;
		}

		public List<PullRequest> getPrs3() {
			return prs3;
		}

		public void setPrs3(List<PullRequest> prs3) {
			this.prs3 = prs3;
		}

		public List<PullRequest> getPrs4() {
			return prs4;
		}

		public void setPrs4(List<PullRequest> prs4) {
			this.prs4 = prs4;
		}

		public List<PullRequest> getPrs5() {
			return prs5;
		}

		public void setPrs5(List<PullRequest> prs5) {
			this.prs5 = prs5;
		}

		public List<PullRequest> getPrs6() {
			return prs6;
		}

		public void setPrs6(List<PullRequest> prs6) {
			this.prs6 = prs6;
		}

		public List<PullRequest> getPrs7() {
			return prs7;
		}

		public void setPrs7(List<PullRequest> prs7) {
			this.prs7 = prs7;
		}

		public List<PullRequest> getPrs8() {
			return prs8;
		}

		public void setPrs8(List<PullRequest> prs8) {
			this.prs8 = prs8;
		}

		public List<IssueData> getIssuedata1() {
			return issuedata1;
		}

		public void setIssuedata1(List<IssueData> issuedata1) {
			this.issuedata1 = issuedata1;
		}

		public List<IssueData> getIssuedata2() {
			return issuedata2;
		}

		public void setIssuedata2(List<IssueData> issuedata2) {
			this.issuedata2 = issuedata2;
		}

		public List<IssueData> getIssuedata3() {
			return issuedata3;
		}

		public void setIssuedata3(List<IssueData> issuedata3) {
			this.issuedata3 = issuedata3;
		}

		public List<IssueData> getIssuedata4() {
			return issuedata4;
		}

		public void setIssuedata4(List<IssueData> issuedata4) {
			this.issuedata4 = issuedata4;
		}

		public List<IssueData> getIssuedata5() {
			return issuedata5;
		}

		public void setIssuedata5(List<IssueData> issuedata5) {
			this.issuedata5 = issuedata5;
		}

		public List<IssueData> getIssuedata6() {
			return issuedata6;
		}

		public void setIssuedata6(List<IssueData> issuedata6) {
			this.issuedata6 = issuedata6;
		}

		public List<IssueData> getIssuedata7() {
			return issuedata7;
		}

		public void setIssuedata7(List<IssueData> issuedata7) {
			this.issuedata7 = issuedata7;
		}

		public List<IssueData> getIssuedata8() {
			return issuedata8;
		}

		public void setIssuedata8(List<IssueData> issuedata8) {
			this.issuedata8 = issuedata8;
		}
		
		static List<IssueData> doSth(GitHubRepository repo1){
			Optional<List<IssueData>> issues1 = repo1.getIssues(true);
			return issues1.get();
			
		}
		
		
	}
