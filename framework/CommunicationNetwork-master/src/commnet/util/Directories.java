package commnet.util;

import java.io.File;

public class Directories {

	private static final String APP_DIR = System.getProperty("user.dir");

	public static String getAppHome() {
		return APP_DIR;
	}

	public static File getReposDir() {
		return getDir("repos");
	}

	public static File getLogDir() {
		return getDir("logs");
	}
	
	public static File getUnusedMergeCommitsDir() {
		return getDir("unused-commits");
	}
	
	public static File getNonUpdatedCommitsDir() {
		return getDir("non-updated-commits");
	}

	/**
	 * Get a File Name and creates a Directory if necessary
	 * 
	 * @param child
	 * @return
	 */
	private static File getDir(String child) {
		File dir;
		if (!(dir = new File(new File(APP_DIR, "CN-files"), child)).exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
