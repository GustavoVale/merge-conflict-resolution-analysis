package commnet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import commnet.crawler.NCThreadPoolExecutor;
import commnet.crawler.RepositoryCrawler;
import commnet.model.enums.NetworkType;
import commnet.util.IOHandler;
import commnet.util.Logger;

public class CN_Main {

	private static NetworkType netType;

	public static void main(String[] args) {

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();

		options.addOption(Option.builder("all").longOpt("runAll")
				.desc("This mode build merge scenarios, issues, and networks. The only function it does not run is "
						+ "metric extraction once it will be interesting after computing merge scenarios, issues, and networks.")
				.build());

		options.addOption(Option.builder("net").longOpt("Networks")
				.desc("build networks with the data already set in the database.").build());
		options.addOption(Option.builder("contcomnet").longOpt("ContributionAndComprehensiveNetworks")
				.desc("build contribution and comprehensive networks with the data already set in the database.")
				.build());
		options.addOption(Option.builder("contnet").longOpt("ContributionNetworks")
				.desc("build contribution networks with the data already set in the database.").build());
		options.addOption(Option.builder("prenet").longOpt("PreciseCommunicationNetworks")
				.desc("build precise communication networks with the data already set in the database.").build());
		options.addOption(Option.builder("compnet").longOpt("ComprehensiveCommunicationNetworks")
				.desc("build comprehensive communication networks with the data already set in the database.").build());
		options.addOption(Option.builder("artnet").longOpt("ChangedArtifactCommunicationNetworks")
				.desc("build changed artifact communication networks with the data already set in the database.")
				.build());
		options.addOption(Option.builder("comnet").longOpt("CommunicationNetworks")
				.desc("build communication networks with the data already set in the database.").build());

		options.addOption(Option.builder("ms").longOpt("MergeScenarios").desc("build merge scenarios.").build());
		options.addOption(Option.builder("i").longOpt("issues").desc("build issues and pull requests.").build());

		options.addOption(Option.builder("mepr").longOpt("ProjectMetrics")
				.desc("extract metrics related to the target projects.").build());
		options.addOption(Option.builder("menet").longOpt("NetworkMetrics")
				.desc("extract metrics related to the target projects.").build());
		options.addOption(Option.builder("mems").longOpt("MergeMetrics")
				.desc("extract metrics related to the target projects.").build());
		options.addOption(Option.builder("mefi").longOpt("FileMetrics")
				.desc("extract metrics related to the target projects.").build());
		options.addOption(Option.builder("mech").longOpt("ChunkMetrics")
				.desc("extract metrics related to the target projects.").build());
		options.addOption(Option.builder("meall").longOpt("AllMetrics")
				.desc("extract metrics related to the target projects.").build());

		options.addOption(Option.builder("compCommunicators").longOpt("StoreCommunicatorsByComprehensiveApproach")
				.desc("Set a list of all communicators of the merge scenarios of a project by using the comprehensive approach.")
				.build());
		options.addOption(Option.builder("precCommunicators").longOpt("StoreCommunicatorsByPreciseApproach")
				.desc("Set a list of all communicators of the merge scenarios of a project by using the precise approach.")
				.build());
		options.addOption(Option.builder("artCommunicators").longOpt("StoreCommunicatorsByChangedArtifactApproach")
				.desc("Set a list of all communicators of the merge scenarios of a project by using the changed-artifacts approach.")
				.build());
		options.addOption(Option.builder("storeDevs").longOpt("StoreDevsIntoDB")
				.desc("Set a list of developers that contribute to merge scenarios of a project with some metrics to measure the size of the contribuiton.")
				.build());
		options.addOption(Option.builder("storeCommitters").longOpt("StoreCommittersIntoDB")
				.desc("Set committers for each merge scenarios of a project.").build());
		options.addOption(Option.builder("storeIntegrators").longOpt("StoreIntegratorsIntoDB")
				.desc("Set integrators for each merge scenarios of a project.").build());
		
		options.addOption(Option.builder("mci").longOpt("MergeConflictInfo")
				.desc("get information from merge conflicts and set in the database.").build());
		
		options.addOption(Option.builder("mcm").longOpt("MergeConflictMetrics")
				.desc("get information from merge conflicts and set in the database.").build());

		options.addOption("h", "help", false, "Print this help page");

		File reposListFile;
		File tokensFile;
		File mergeCommitsFile;

		try {
			CommandLine cmd = parser.parse(options, args);
			// user is looking for help
			if (cmd.hasOption("h")) {
				new HelpFormatter().printHelp("java ", options);
				System.exit(0);
			}

			else if (cmd.hasOption("all") || cmd.hasOption("net") || cmd.hasOption("contcomnet")
					|| cmd.hasOption("contnet") || cmd.hasOption("prenet") || cmd.hasOption("compnet")
					|| cmd.hasOption("artnet") || cmd.hasOption("comnet") || cmd.hasOption("ms") || cmd.hasOption("i")
					|| cmd.hasOption("mepr") || cmd.hasOption("menet") || cmd.hasOption("mems") || cmd.hasOption("mefi")
					|| cmd.hasOption("mech") || cmd.hasOption("meall") || cmd.hasOption("compCommunicators")
					|| cmd.hasOption("precCommunicators") || cmd.hasOption("artCommunicators")
					|| cmd.hasOption("storeDevs") || cmd.hasOption("storeCommitters")
					|| cmd.hasOption("storeIntegrators") || cmd.hasOption("mci") || cmd.hasOption("mcm")) {

				String urlsFilePath;
				String tokenFilePath;
				String mergeCommitsPath;
				// NetworkType netType;
				if (cmd.hasOption("all")) {
					netType = NetworkType.ALL;
				}
				if (cmd.hasOption("net")) {
					netType = NetworkType.NETWORKS;
				}
				if (cmd.hasOption("contcomnet")) {
					netType = NetworkType.CONTCOMNETWORKS;
				}
				if (cmd.hasOption("contnet")) {
					netType = NetworkType.CONTRIBUTIONNETWORKS;
				}
				if (cmd.hasOption("compnet")) {
					netType = NetworkType.COMPREHENSIVENETWORKS;
				}
				if (cmd.hasOption("prenet")) {
					netType = NetworkType.PRECISENETWORKS;
				}
				if (cmd.hasOption("artnet")) {
					netType = NetworkType.CHANGEDARTIFACTNETWORKS;
				}
				if (cmd.hasOption("comnet")) {
					netType = NetworkType.COMMUNICATIONNETWORKS;
				}
				if (cmd.hasOption("ms")) {
					netType = NetworkType.MERGESCENARIO;
				}
				if (cmd.hasOption("i")) {
					netType = NetworkType.ISSUE;
				}
				if (cmd.hasOption("meall")) {
					netType = NetworkType.ALLMETRICS;
				}
				if (cmd.hasOption("mepr")) {
					netType = NetworkType.PROJECTMETRICS;
				}
				if (cmd.hasOption("menet")) {
					netType = NetworkType.NETWORKMETRICS;
				}
				if (cmd.hasOption("mems")) {
					netType = NetworkType.MERGEMETRICS;
				}
				if (cmd.hasOption("mefi")) {
					netType = NetworkType.FILEMETRICS;
				}
				if (cmd.hasOption("mech")) {
					netType = NetworkType.CHUNKMETRICS;
				}
				if (cmd.hasOption("compCommunicators")) {
					netType = NetworkType.COMPREHENSIVECOMMUNICATORS;
				}
				if (cmd.hasOption("precCommunicators")) {
					netType = NetworkType.PRECISECOMMUNICATORS;
				}
				if (cmd.hasOption("artCommunicators")) {
					netType = NetworkType.CHANGEDARTIFACTCOMMUNICATORS;
				}
				if (cmd.hasOption("storeDevs")) {
					netType = NetworkType.DEVS;
				}
				if (cmd.hasOption("storeCommitters")) {
					netType = NetworkType.COMMITTERS;
				}
				if (cmd.hasOption("storeIntegrators")) {
					netType = NetworkType.INTEGRATORS;
				}
				if (cmd.hasOption("mci")) {
					netType = NetworkType.MERGECONFLICTINFO;
				}
				if (cmd.hasOption("mcm")) {
					netType = NetworkType.MERGECONFLICTMETRICS;
				}

				String[] remainingArgs = cmd.getArgs();
				if (remainingArgs.length == 0) {
					throw new IllegalArgumentException("Path to UrlsFile must be provided!");
				}

				urlsFilePath = remainingArgs[0];
				tokenFilePath = remainingArgs[1];
				mergeCommitsPath = remainingArgs[2];
				reposListFile = new File(urlsFilePath);
				tokensFile = new File(tokenFilePath);
				mergeCommitsFile = new File(mergeCommitsPath);

				// Ends execution if file with repos is not found.
				if (!reposListFile.exists()) {
					throw new IllegalArgumentException(
							"CONTRIBUTION NETWORK ended without retrieving any repository.\n\n"
									+ "The file containing the repository's URL of the target systems was not found. "
									+ "Check whether the file \"" + urlsFilePath + "\" exists.");
				}

				// Ends execution if file with tokens is not found.
				if (!tokensFile.exists()) {
					throw new IllegalArgumentException(
							"COMMUNICATION NETWORK ended without retrieving any repository.\n\n"
									+ "The file containing the GitHub tokens was not found. "
									+ "Check whether the file \"" + tokenFilePath + "\" exists.");
				}

				// Ends execution if file with merge commits is not found.
				if (!mergeCommitsFile.exists()) {
					throw new IllegalArgumentException("TOOL ended without retrieving any repository.\n\n"
							+ "The file containing the merge commits was not found. " + "Check whether the file \""
							+ mergeCommitsPath + "\" exists.");
				}

				MainThread m = new MainThread(tokensFile, reposListFile, mergeCommitsFile);
				m.start();
				m.join();
				Logger.log("CONTRIBUTION NETWORK finished. Files rewritten.");

			} else {
				throw new IllegalArgumentException("CONTRIBUTION NETWORK ended without retrieving any repository.\n\n"
						+ "You should use '-h' if you are looking for help. "
						+ "Otherwise, at least one of the '-i' or '-pr' or '-m' options are mandatory.");
			}

		} catch (ParseException e) {
			new HelpFormatter().printHelp("java ", options);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	static class MainThread extends Thread {
		private File repoList;
		private File tokenList;
		private File commitList;

		MainThread(File token, File reposListFile, File mergeCommitFile) {
			this.repoList = reposListFile;
			this.tokenList = token;
			this.commitList = mergeCommitFile;
		}

		public void run() {

			IOHandler io = new IOHandler();
			IOHandler iohandler = new IOHandler();
			IOHandler ioCommithandler = new IOHandler();
			List<String> tokens = iohandler.readFile(tokenList);
			// responsible to coordinate the threads for each system
			NCThreadPoolExecutor pool = new NCThreadPoolExecutor();
			List<String> projects = io.readFile(repoList);
			List<String> mergeCommits = ioCommithandler.readFile(commitList);

			for (String url : projects) {
				try {
					pool.runTask(new RepositoryCrawler(url, tokens, mergeCommits, netType));
				} catch (IOException e) {
					Logger.logStackTrace(e);
					throw new RuntimeException(e);
				}
				Logger.log("Repository scheduled: " + url);
			}

			pool.shutDown();
		}

	}

}
