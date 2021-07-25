package commnet.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IOHandler {

	/**
	 * Read a given file
	 * 
	 * @param fin
	 * @return
	 */
	public List<String> readFile(File fin) {
		List<String> lines = null;
		try (FileInputStream fis = new FileInputStream(fin)) {

			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			lines = new ArrayList<>();
			String line = null;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			br.close();
			fis.close();

		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

		return lines;
	}

	/**
	 * Create a new line to the file
	 * 
	 * @param file
	 * @param aLine
	 * @throws IOException
	 */
	public void appendLineToFile(File file, String aLine) throws IOException {
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(createFile(file), true));
		writer.write(aLine + "\n");
		// Close the writer regardless of what happens...
		writer.close();
	}

	/**
	 * Create a new file
	 * 
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	private File createFile(File directory) throws IOException {
		if (!directory.getParentFile().exists()) {
			directory.getParentFile().mkdirs();
		}
		directory.createNewFile();
		return directory;
	}
}
