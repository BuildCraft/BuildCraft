package buildcraft.core.blueprints;

import buildcraft.core.proxy.CoreProxy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.TreeMap;

public class BptPlayerIndex {

	private TreeMap<String, File> bluePrintsFile = new TreeMap<String, File>();

	private File baseDir;
	private File file;

	public BptPlayerIndex(String filename, BptRootIndex rootIndex) throws IOException {
		baseDir = new File("./", "blueprints/");
		file = new File(baseDir, filename);
		baseDir.mkdir();

		if (!file.exists()) {
			file.createNewFile();

			for (String file : rootIndex.filesSet.keySet()) {
				bluePrintsFile.put(file, new File(baseDir, file));
			}

			saveIndex();
		} else {
			loadIndex();
		}
	}

	public void loadIndex() throws IOException {
		FileInputStream input = new FileInputStream(file);

		BufferedReader reader = new BufferedReader(new InputStreamReader(input, "8859_1"));

		while (true) {
			String line = reader.readLine();

			if (line == null) {
				break;
			}

			line = line.replaceAll("\\n", "");

			File bptFile = new File(baseDir, line);

			bluePrintsFile.put(line, bptFile);
		}

		input.close();
	}

	public void addBlueprint(File file) throws IOException {
		bluePrintsFile.put(file.getName(), file);

		saveIndex();
	}

	public void saveIndex() throws IOException {
		FileOutputStream output = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "8859_1"));

		for (String line : bluePrintsFile.keySet()) {
			writer.write(line);
			writer.newLine();
		}

		writer.flush();
		output.close();
	}

	public void deleteBluePrint(String fileName) {
		bluePrintsFile.remove(fileName);

		try {
			saveIndex();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String nextBpt(String name) {
		if (bluePrintsFile.size() == 0)
			return null;
		else if (name == null)
			return bluePrintsFile.firstKey();
		else
			return bluePrintsFile.higherKey(name);
	}

	public String prevBpt(String name) {
		if (bluePrintsFile.size() == 0)
			return null;
		else if (name == null)
			return bluePrintsFile.lastKey();
		else
			return bluePrintsFile.lowerKey(name);
	}
}
