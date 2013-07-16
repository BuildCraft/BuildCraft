/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

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

public class BptRootIndex {

	private TreeMap<Integer, File> bluePrintsFile = new TreeMap<Integer, File>();
	public TreeMap<String, Integer> filesSet = new TreeMap<String, Integer>();

	private TreeMap<Integer, BptBase> bluePrints = new TreeMap<Integer, BptBase>();

	private File baseDir;
	private File file;

	public int maxBpt = 0;

	public BptRootIndex(String filename) throws IOException {
		baseDir = new File("./", "blueprints/");
		file = new File(baseDir, filename);
		baseDir.mkdir();

		if (!file.exists()) {
			file.createNewFile();
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

			maxBpt++;

			filesSet.put(line, maxBpt);

			if (bptFile.exists()) {
				bluePrintsFile.put(maxBpt, bptFile);
			}

		}

		input.close();

		saveIndex();
	}

	public void importNewFiles() throws IOException {
		String files[] = baseDir.list();

		for (String foundFile : files) {
			String[] parts = foundFile.split("[.]");

			if (parts.length < 2 || !parts[1].equals("bpt")) {
				continue;
			}

			if (!filesSet.containsKey(foundFile)) {
				maxBpt++;
				filesSet.put(foundFile, maxBpt);

				File newFile = new File(baseDir, foundFile);

				bluePrintsFile.put(maxBpt, newFile);

				// for (BptPlayerIndex playerIndex : BuildCraftBuilders.playerLibrary.values())
				// playerIndex.addBlueprint(newFile);
			}
		}

		saveIndex();
	}

	public void saveIndex() throws IOException {
		FileOutputStream output = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "8859_1"));

		for (int i = 1; i <= maxBpt; ++i) {
			File f = bluePrintsFile.get(i);

			if (f != null) {
				writer.write(f.getName());
			}

			writer.newLine();
		}

		writer.flush();
		output.close();
	}

	public BptBase getBluePrint(int number) {
		if (!bluePrints.containsKey(number))
			if (bluePrintsFile.containsKey(number)) {
				BptBase bpt = BptBase.loadBluePrint(bluePrintsFile.get(number), number);

				if (bpt != null) {
					bluePrints.put(number, bpt);
					bpt.file = bluePrintsFile.get(number);
				} else {
					bluePrintsFile.remove(number);
					return null;
				}
			}

		return bluePrints.get(number);
	}

	public BptBase getBluePrint(String filename) {
		return getBluePrint(filesSet.get(filename));
	}

	public int storeBluePrint(BptBase bluePrint) {
		String name = bluePrint.name;

		if (name == null || name.equals("")) {
			name = "unnamed";
		}

		if (filesSet.containsKey(name + ".bpt")) {
			int n = 0;

			while (filesSet.containsKey(name + "_" + n + ".bpt")) {
				n++;
			}

			name = name + "_" + n;
		}

		maxBpt++;

		filesSet.put(name + ".bpt", maxBpt);

		name = name + ".bpt";

		File bptFile = new File(baseDir, name);

		bluePrintsFile.put(maxBpt, bptFile);
		bluePrints.put(maxBpt, bluePrint);
		bluePrint.file = bptFile;
		bluePrint.save();
		bluePrint.position = maxBpt;

		try {
			saveIndex();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return maxBpt;
	}
}
