package commnet.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.logging.Level;

public abstract class Logger {

	private static File log = new File(Directories.getLogDir(), "cn.log");
//	private static java.util.logging.Logger utilLogger = java.util.logging.Logger.getLogger(Logger.class.getCanonicalName());

	public static synchronized void log(String message) {
		log(log, message);
	}

	public static File getLog() {
		return log;
	}

	/**
	 * Logs a message in a given file.
	 * 
	 * @param logFile
	 * @param message
	 */
	public static void log(File logFile, String message) {
//		utilLogger.info(message);
		IOHandler io = new IOHandler();
		try {
			logFile = new File(logFile.getParentFile(), logFile.getName());
			io.appendLineToFile(logFile, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs a given exception stack trace in the given file.
	 * 
	 * @param logFile
	 * @param anException
	 */
	public static synchronized void logStackTrace(File logFile, Exception anException) {
//		utilLogger.log(Level.WARNING, "An exeception occurred.", anException);
		StringBuilder sb = new StringBuilder();
		String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		sb.append(">>>>> Exception Begin: (" + timestamp + ")\n");
		sb.append(getStackTrace(anException));
		sb.append("<<<<< Exception End.");
		log(logFile, sb.toString());
	}

	/**
	 * Logs a given exception stack trace in the given file.
	 * 
	 * @param anException
	 */
	public static synchronized void logStackTrace(Exception anException) {
		logStackTrace(log, anException);
	}

	/**
	 * Writes Exception in the log file
	 * 
	 * @param anException
	 * @return Exception
	 */
	private static String getStackTrace(Exception anException) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		anException.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

}
