package commnet.extra;

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
import commnet.crawler.NewRepCrawler;
import commnet.model.enums.ExtraMode;
import commnet.util.IOHandler;
import commnet.util.Logger;

public class Extra_Main {

	private static ExtraMode extraMode;

	public static void main(String[] args) {

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();

		options.addOption(
				Option.builder("commitUpdater").longOpt("UpdateCommitsDate").desc("Update the commit date.").build());
		options.addOption(
				Option.builder("edgeRemover").longOpt("RemoveEdgeType").desc("Remove specific type of edges").build());
		options.addOption(Option.builder("devRemover").longOpt("RemoveDevelopers")
				.desc("Remove specific data related to developers").build());
		options.addOption(
				Option.builder("issueRemover").longOpt("RemoveIssues").desc("Remove issues from a project").build());
		options.addOption(Option.builder("nucr").longOpt("NonUpdatedCommitsRetriever")
				.desc("Retrieve commits that the date were not updated").build());
		options.addOption(Option.builder("projMeRemover").longOpt("RemoveProjectMetrics")
				.desc("Remove project_metrics from a project").build());
		options.addOption(Option.builder("netMeRemover").longOpt("RemoveNetMetrics")
				.desc("Remove net_metrics from a project").build());
		options.addOption(Option.builder("msMeRemover").longOpt("RemoveMSMetrics")
				.desc("Remove ms_metrics from a project").build());
		options.addOption(Option.builder("fileMeRemover").longOpt("RemoveFileMetrics")
				.desc("Remove file_metrics from a project").build());
		options.addOption(Option.builder("chunkMeRemover").longOpt("RemoveChunkMetrics")
				.desc("Remove chunk_metrics from a project").build());
		options.addOption(Option.builder("commitFieldsUpdater").longOpt("UpdateCommitFieldsInDatabase")
				.desc("Update commit fields for all commits of a project in the database").build());

		File reposListFile;
		File extraFile;

		try {

			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("h")) {
				new HelpFormatter().printHelp("java ", options);
				System.exit(0);
			}

			else if (cmd.hasOption("commitUpdater") || cmd.hasOption("edgeRemover") || cmd.hasOption("devRemover")
					|| cmd.hasOption("issueRemover") || cmd.hasOption("nucr") || cmd.hasOption("projMeRemover") 
					|| cmd.hasOption("netMeRemover") || cmd.hasOption("msMeRemover") || cmd.hasOption("fileMeRemover") 
					|| cmd.hasOption("chunkMeRemover") || cmd.hasOption("commitFieldsUpdater")) {

				String urlsFilePath;
				String extraPath;

				if (cmd.hasOption("commitUpdater")) {
					extraMode = ExtraMode.COMMITUPDATER;
				}
				if (cmd.hasOption("edgeRemover")) {
					extraMode = ExtraMode.EDGEREMOVER;
				}
				if (cmd.hasOption("devRemover")) {
					extraMode = ExtraMode.DEVELOPERREMOVER;
				}
				if (cmd.hasOption("issueRemover")) {
					extraMode = ExtraMode.ISSUEREMOVER;
				}
				if (cmd.hasOption("nucr")) {
					extraMode = ExtraMode.NONUPDATEDCOMMITRETRIEVER;
				}
				if (cmd.hasOption("projMeRemover")) {
					extraMode = ExtraMode.PROJECTMETRICREMOVER;
				}
				if (cmd.hasOption("netMeRemover")) {
					extraMode = ExtraMode.NETMETRICREMOVER;
				}
				if (cmd.hasOption("msMeRemover")) {
					extraMode = ExtraMode.MSMETRICREMOVER;
				}
				if (cmd.hasOption("fileMeRemover")) {
					extraMode = ExtraMode.FILEMETRICREMOVER;
				}
				if (cmd.hasOption("chunkMeRemover")) {
					extraMode = ExtraMode.CHUNKMETRICREMOVER;
				}
				if (cmd.hasOption("commitFieldsUpdater")) {
					extraMode = ExtraMode.COMMITFIELDSUPDATER;
				}

				String[] remainingArgs = cmd.getArgs();
				if (remainingArgs.length == 0) {
					throw new IllegalArgumentException("Path to UrlsFile must be provided!");
				}

				urlsFilePath = remainingArgs[0];
				extraPath = remainingArgs[1];
				reposListFile = new File(urlsFilePath);
				extraFile = new File(extraPath);

				// Ends execution if file with repos is not found.
				if (!reposListFile.exists()) {
					throw new IllegalArgumentException(
							"CONTRIBUTION NETWORK ended without retrieving any repository.\n\n"
									+ "The file containing the repository's URL of the target systems was not found. "
									+ "Check whether the file \"" + urlsFilePath + "\" exists.");
				}

				// Ends execution if file with extra is not found.
				if (!extraFile.exists()) {
					throw new IllegalArgumentException(
							"CONTRIBUTION NETWORK ended without retrieving any repository.\n\n"
									+ "The file containing the extra data to run the target tool mode was not found. "
									+ "Check whether the file \"" + extraPath + "\" exists.");
				}

				MainThread m = new MainThread(reposListFile, extraFile);
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
		private File extra;

		MainThread(File reposListFile, File extraFile) {
			this.repoList = reposListFile;
			this.extra = extraFile;
		}

		public void run() {

			IOHandler io = new IOHandler();
			IOHandler ioExtra = new IOHandler();
			// responsible to coordinate the threads for each system
			NCThreadPoolExecutor pool = new NCThreadPoolExecutor();
			List<String> projects = io.readFile(repoList);
			List<String> extraData = ioExtra.readFile(extra);

			for (String url : projects) {
				try {
					pool.runTask(new NewRepCrawler(url, extraData, extraMode));
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
