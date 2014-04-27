/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.blueprints.BlueprintId.Kind;
import buildcraft.core.blueprints.BlueprintBase;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlueprintDatabase {
	private final int bufferSize = 8192;
	private final static String BPT_EXTENSION = ".bpt";
	private final static String TPL_EXTENSION = ".tpl";
	private File blueprintFolder;
	private final static int PAGE_SIZE = 12;

	private Set<BlueprintId> blueprintIds = new TreeSet<BlueprintId>();
	private BlueprintId[] pages = new BlueprintId[0];

	/**
	 * Initialize the blueprint database.
	 *
	 * @param configDir config directory to read the blueprints from.
	 */
	public void init(File configDir) {
		blueprintFolder = configDir;

		if (!blueprintFolder.exists()) {
			blueprintFolder.mkdirs();
		}

		loadIndex();
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

	public void deleteBlueprint(BlueprintId id) {
		File blueprintFile = getBlueprintFile(id);
		blueprintFile.delete();
		blueprintIds.remove(id);
		pages = new BlueprintId[blueprintIds.size()];
		pages = blueprintIds.toArray(pages);
	}

	private BlueprintId save(BlueprintBase blueprint) {
		blueprint.id.generateUniqueId(blueprint.getData());

		BlueprintId id = blueprint.id;
		File blueprintFile = getBlueprintFile(id);

		if (!blueprintFile.exists()) {
			OutputStream gzOs = null;
			try {
				FileOutputStream f = new FileOutputStream(blueprintFile);
				f.write(blueprint.getData());
				f.close();
			} catch (IOException ex) {
				Logger.getLogger("Buildcraft").log(Level.SEVERE, String.format("Failed to save Blueprint file: %s %s", blueprintFile.getName(), ex.getMessage()));
			} finally {
				try {
					if (gzOs != null) {
						gzOs.close();
					}
				} catch (IOException e) {
				}
			}
		}

		return id;
	}

	private File getBlueprintFile(BlueprintId id) {
		if (id.kind == Kind.Blueprint) {
			return new File(blueprintFolder, String.format(Locale.ENGLISH, "%s" + BPT_EXTENSION, id.toString()));
		} else {
			return new File(blueprintFolder, String.format(Locale.ENGLISH, "%s" + TPL_EXTENSION, id.toString()));
		}
	}

	private void loadIndex() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(BPT_EXTENSION) || name.endsWith(TPL_EXTENSION);
			}
		};

		for (File blueprintFile : blueprintFolder.listFiles(filter)) {
			String fileName = blueprintFile.getName();

			int cutIndex = fileName.indexOf(BuildCraftBuilders.BPT_SEP_CHARACTER);

			String prefix = fileName.substring(0, cutIndex);
			String suffix = fileName.substring(cutIndex + 1);

			BlueprintId id = new BlueprintId();
			id.name = prefix;

			if (suffix.contains(BPT_EXTENSION)) {
				id.uniqueId = BlueprintId.toBytes(suffix.replaceAll(BPT_EXTENSION, ""));
				id.kind = Kind.Blueprint;
			} else {
				id.uniqueId = BlueprintId.toBytes(suffix.replaceAll(TPL_EXTENSION, ""));
				id.kind = Kind.Template;
			}

			if (!blueprintIds.contains(id)) {
				blueprintIds.add(id);
			}
		}

		pages = blueprintIds.toArray(pages);
	}

	public boolean exists(BlueprintId id) {
		return blueprintIds.contains(id);
	}

	public BlueprintBase load(final BlueprintId id) {
		if (id == null) {
			return null;
		}

		File blueprintFile = getBlueprintFile(id);

		if (blueprintFile.exists()) {
			try {
				FileInputStream f = new FileInputStream(blueprintFile);
				byte[] data = new byte[(int) blueprintFile.length()];
				f.read(data);
				f.close();

				NBTTagCompound nbt = CompressedStreamTools.decompress(data);

				BlueprintBase blueprint = BlueprintBase.loadBluePrint(nbt);
				blueprint.setData(data);
				blueprint.id = id;

				return blueprint;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public ArrayList<BlueprintId> getPage(int pageId) {
		ArrayList<BlueprintId> result = new ArrayList<BlueprintId>();

		if (pageId < 0) {
			return result;
		}

		for (int i = pageId * PAGE_SIZE; i < pageId * PAGE_SIZE + PAGE_SIZE; ++i) {
			if (i < pages.length) {
				result.add(pages[i]);
			} else {
				break;
			}
		}

		return result;
	}

	public int getPageNumber() {
		return (int) Math.ceil((float) blueprintIds.size() / (float) PAGE_SIZE);
	}
}
