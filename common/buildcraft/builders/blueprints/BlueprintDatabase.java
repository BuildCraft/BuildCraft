/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.BuildCraftBuilders;
import buildcraft.core.blueprints.BlueprintBase;

public class BlueprintDatabase {
	private final int bufferSize = 8192;
	private final String fileExt = ".bpt";
	private File blueprintFolder;
	private final static int PAGE_SIZE = 12;

	private Set <BlueprintId> blueprintIds = new TreeSet<BlueprintId> ();
	private BlueprintId [] pages = new BlueprintId [0];

	private Map<BlueprintId, BlueprintBase> loadedBlueprints = new WeakHashMap<BlueprintId, BlueprintBase>();

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
	 * Get a specific blueprint by id.
	 *
	 * @note The blueprint will be loaded as needed.
	 *
	 * @param id blueprint id
	 * @return blueprint or null if it can't be retrieved
	 */
	public BlueprintBase get(BlueprintId id) {
		BlueprintBase ret = loadedBlueprints.get(id);

		if (ret == null) {
			ret = load(id);
		}

		return ret;
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

		if (!loadedBlueprints.containsKey(id)) {
			loadedBlueprints.put(id, blueprint);
		}

		return id;
	}

	private BlueprintId save(BlueprintBase blueprint) {
		blueprint.id.generateUniqueId(blueprint.getData());

		BlueprintId id = blueprint.id;

		File blueprintFile = new File(blueprintFolder, String.format(Locale.ENGLISH, "%s" + fileExt, id.toString()));

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
				} catch (IOException e) { }
			}
		}

		return id;
	}

	private void loadIndex() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(fileExt);
			}
		};

		for (File blueprintFile : blueprintFolder.listFiles(filter)) {
			String fileName = blueprintFile.getName();

			int cutIndex = fileName.indexOf(BuildCraftBuilders.BPT_SEP_CHARACTER);

			String prefix = fileName.substring(0, cutIndex);
			String suffix = fileName.substring(cutIndex + 1);

			BlueprintId id = new BlueprintId();
			id.name = prefix;
			id.uniqueId = BlueprintId.toBytes (suffix.replaceAll(".bpt", ""));

			if (!blueprintIds.contains(id)) {
				blueprintIds.add(id);
			}
		}

		pages = blueprintIds.toArray(pages);
	}

	private BlueprintBase load(final BlueprintId id) {
		File blueprintFile = new File(blueprintFolder, String.format(
				Locale.ENGLISH, "%s" + fileExt, id.toString()));

		if (blueprintFile.exists()) {
			try {
				FileInputStream f = new FileInputStream(blueprintFile);
				byte [] data = new byte [(int) blueprintFile.length()];
				f.read (data);
				f.close();

				NBTTagCompound nbt = CompressedStreamTools.decompress(data);

				BlueprintBase blueprint = BlueprintBase.loadBluePrint(nbt);
				blueprint.setData(data);
				blueprint.id = id;

				loadedBlueprints.put(id, blueprint);

				return blueprint;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
}
