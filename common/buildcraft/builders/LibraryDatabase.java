/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.BCLog;
import buildcraft.api.library.LibraryAPI;
import buildcraft.core.blueprints.LibraryId;
import buildcraft.core.lib.utils.NBTUtils;

public class LibraryDatabase {
	protected Set<LibraryId> blueprintIds;
	protected LibraryId[] pages = new LibraryId[0];

	private File outputDir;
	private List<File> inputDirs;

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

		inputDirs = new ArrayList<File>();

		for (int i = 0; i < inputPaths.length; ++i) {
			File inputDir = new File(inputPaths[i]);
			if (inputDir.exists()) {
				inputDirs.add(inputDir);
			}
		}

		refresh();
	}

	public void refresh() {
		blueprintIds = new TreeSet<LibraryId>();
		for (File f : inputDirs) {
			loadIndex(f);
		}
	}

	public void deleteBlueprint(LibraryId id) {
		File blueprintFile = getBlueprintFile(id);

		if (blueprintFile != null) {
			blueprintFile.delete();
			blueprintIds.remove(id);
			pages = new LibraryId[blueprintIds.size()];
			pages = blueprintIds.toArray(pages);
		}
	}

	protected File getBlueprintFile(LibraryId id) {
		String name = String.format(Locale.ENGLISH, "%s." + id.extension, id.toString());

		for (File dir : inputDirs) {
			File f = new File(dir, name);

			if (f.exists()) {
				return f;
			}
		}

		return null;
	}

	protected File getBlueprintOutputFile(LibraryId id) {
		String name = String.format(Locale.ENGLISH, "%s." + id.extension, id.toString());

		return new File(outputDir, name);
	}

	public void add(LibraryId base, NBTTagCompound compound) {
		save(base, compound);

		if (!blueprintIds.contains(base)) {
			blueprintIds.add(base);
			pages = blueprintIds.toArray(pages);
		}
	}

	private void save(LibraryId base, NBTTagCompound compound) {
		byte[] data = NBTUtils.save(compound);
		base.generateUniqueId(data);
		File blueprintFile = getBlueprintOutputFile(base);

		if (!blueprintFile.exists()) {
			try {
				FileOutputStream f = new FileOutputStream(blueprintFile);
				f.write(data);
				f.close();
			} catch (IOException ex) {
				BCLog.logger.error(String.format("Failed to save library file: %s %s", blueprintFile.getName(), ex.getMessage()));
			}
		}
	}

	private void loadIndex(File directory) {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				int dotIndex = name.lastIndexOf('.') + 1;
				String extension = name.substring(dotIndex);
				return LibraryAPI.getHandlerFor(extension) != null;
			}
		};

		if (directory.exists()) {
			File[] files = directory.listFiles(filter);
			if (files == null || files.length == 0) {
				return;
			}

			for (File blueprintFile : files) {
				String fileName = blueprintFile.getName();

				LibraryId id = new LibraryId();

				int sepIndex = fileName.lastIndexOf(LibraryId.BPT_SEP_CHARACTER);
				int dotIndex = fileName.lastIndexOf('.');

				if (dotIndex > 0) {
					String extension = fileName.substring(dotIndex + 1);

					if (sepIndex > 0) {
						String prefix = fileName.substring(0, sepIndex);
						String suffix = fileName.substring(sepIndex + 1);

						id.name = prefix;
						id.uniqueId = LibraryId.toBytes(suffix.substring(0, suffix.length() - (extension.length() + 1)));
					} else {
						id.name = fileName.substring(0, dotIndex);
						id.uniqueId = new byte[0];
					}
					id.extension = extension;

					if (!blueprintIds.contains(id)) {
						blueprintIds.add(id);
					}
				} else {
					BCLog.logger.warn("Found incorrectly named (no extension) blueprint file: '%s'!", fileName);
				}
			}

			pages = blueprintIds.toArray(new LibraryId[blueprintIds.size()]);
		}
	}

	public boolean exists(LibraryId id) {
		return blueprintIds.contains(id);
	}

	public NBTTagCompound load(final LibraryId id) {
		if (id == null) {
			return null;
		}

		NBTTagCompound compound = load(getBlueprintFile(id));
		return compound;
	}

	public static NBTTagCompound load(File blueprintFile) {
		if (blueprintFile != null && blueprintFile.exists()) {
			try {
				FileInputStream f = new FileInputStream(blueprintFile);
				byte[] data = new byte[(int) blueprintFile.length()];
				f.read(data);
				f.close();

				return NBTUtils.load(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public List<LibraryId> getBlueprintIds() {
		return Collections.unmodifiableList(new ArrayList<LibraryId>(blueprintIds));
	}
}
