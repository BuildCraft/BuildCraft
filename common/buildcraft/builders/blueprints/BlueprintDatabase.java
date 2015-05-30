/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.BCLog;
import buildcraft.builders.blueprints.BlueprintId.Kind;
import buildcraft.core.blueprints.BlueprintBase;

public class BlueprintDatabase {
	private static final String BPT_EXTENSION = ".bpt";
	private static final String TPL_EXTENSION = ".tpl";
	private static final int PAGE_SIZE = 12;

	private final int bufferSize = 8192;
	private File outputDir;
	private File[] inputDirs;

	private Set<BlueprintId> blueprintIds;
	private BlueprintId [] pages = new BlueprintId [0];

	/**
	 * Initialize the blueprint database.
	 *
	 * @param inputPaths directories to read the blueprints from.
	 */
	public void init(String[] inputPaths, String outputPath) {
		outputDir = new File(outputPath);

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		inputDirs = new File[inputPaths.length];

		for (int i = 0; i < inputDirs.length; ++i) {
			inputDirs[i] = new File(inputPaths[i]);
		}

		refresh();
	}

	public void refresh() {
		blueprintIds = new TreeSet<BlueprintId>();
		loadIndex(inputDirs);
	}

	/**
	 * Add a blueprint to the database and save it to disk.
	 *
	 * @param blueprint blueprint to add
	 * @return id for the added blueprint
	 */
	public BlueprintId add(BlueprintBase blueprint) {
		BlueprintId id = save(blueprint);

		if (!blueprintIds.contains(id)) {
			blueprintIds.add(id);
			pages = blueprintIds.toArray(pages);
		}

		return id;
	}

	public void deleteBlueprint (BlueprintId id) {
		File blueprintFile = getBlueprintFile(id);

		if (blueprintFile != null) {
			blueprintFile.delete();
			blueprintIds.remove(id);
			pages = new BlueprintId[blueprintIds.size()];
			pages = blueprintIds.toArray(pages);
		}
	}

	private BlueprintId save(BlueprintBase blueprint) {
		blueprint.id.generateUniqueId(blueprint.getData());

		BlueprintId id = blueprint.id;
		File blueprintFile = getBlueprintFile(id, outputDir);

		if (!blueprintFile.exists()) {
			try {
				FileOutputStream f = new FileOutputStream(blueprintFile);
				f.write(blueprint.getData());
				f.close();
			} catch (IOException ex) {
				BCLog.logger.error(String.format("Failed to save Blueprint file: %s %s", blueprintFile.getName(), ex.getMessage()));
			}
		}

		return id;
	}

	private File getBlueprintFile(BlueprintId id) {
		String name = "";

		if (id.kind == Kind.Blueprint) {
			name = String.format(Locale.ENGLISH, "%s" + BPT_EXTENSION, id.toString());
		} else {
			name = String.format(Locale.ENGLISH, "%s" + TPL_EXTENSION, id.toString());
		}

		for (File dir : inputDirs) {
			File f = new File(dir, name);

			if (f.exists()) {
				return f;
			}
		}

		return null;
	}

	private File getBlueprintFile(BlueprintId id, File folder) {
		String name = "";

		if (id.kind == Kind.Blueprint) {
			name = String.format(Locale.ENGLISH, "%s" + BPT_EXTENSION, id.toString());
		} else {
			name = String.format(Locale.ENGLISH, "%s" + TPL_EXTENSION, id.toString());
		}

		return new File(folder, name);
	}

	private void loadIndex(File[] dirs) {
		for (File dir : dirs) {
			if (dir != null) {
				loadIndex(dir);
			}
		}
	}

	private void loadIndex(File directory) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(BPT_EXTENSION) || name.endsWith(TPL_EXTENSION);
			}
		};

		if (directory.exists()) {
			for (File blueprintFile : directory.listFiles(filter)) {
				String fileName = blueprintFile.getName();

				BlueprintId id = new BlueprintId();

				int sepIndex = fileName.lastIndexOf(BuildCraftBuilders.BPT_SEP_CHARACTER);
				int dotIndex = fileName.lastIndexOf('.');

				String extension = fileName.substring(dotIndex);

				if (sepIndex > 0) {
					String prefix = fileName.substring(0, sepIndex);
					String suffix = fileName.substring(sepIndex + 1);

					id.name = prefix;
					id.uniqueId = BlueprintId.toBytes(suffix.substring(0, suffix.length() - 4));
				} else {
					id.name = fileName.substring(0, dotIndex);
					id.uniqueId = new byte[0];
				}

				if (extension.equals(BPT_EXTENSION)) {
					id.kind = Kind.Blueprint;
				} else {
					id.kind = Kind.Template;
				}

				if (!blueprintIds.contains(id)) {
					blueprintIds.add(id);
				}
			}

			pages = blueprintIds.toArray(new BlueprintId[blueprintIds.size()]);
		}
	}

	public boolean exists (BlueprintId id) {
		return blueprintIds.contains(id);
	}

	public BlueprintBase load(final BlueprintId id) {
		if (id == null) {
			return null;
		}

		BlueprintBase bpt = load (getBlueprintFile(id));

		if (bpt != null) {
			bpt.id = id;
		}

		return bpt;
	}

	public static BlueprintBase load (File blueprintFile) {
		if (blueprintFile != null && blueprintFile.exists()) {
			try {
				FileInputStream f = new FileInputStream(blueprintFile);
				byte [] data = new byte [(int) blueprintFile.length()];
				f.read (data);
				f.close();

				return load(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static BlueprintBase load(byte[] data) {
		try {
			//Ugly but will probably work
			NBTTagCompound nbt = CompressedStreamTools.read(new DataInputStream(new ByteArrayInputStream(data)), NBTSizeTracker.INFINITE);

			BlueprintBase blueprint = BlueprintBase.loadBluePrint(nbt);
			blueprint.setData(data);

			return blueprint;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ArrayList<BlueprintId> getPage (int pageId) {
		ArrayList<BlueprintId> result = new ArrayList<BlueprintId>();

		if (pageId < 0) {
			return result;
		}

		for (int i = pageId * PAGE_SIZE; i < pageId * PAGE_SIZE + PAGE_SIZE; ++i) {
			if (i < pages.length) {
				result.add(pages [i]);
			} else {
				break;
			}
		}

		return result;
	}

	public int getPageNumber () {
		return (int) Math.ceil((float) blueprintIds.size() / (float) PAGE_SIZE);
	}
}
