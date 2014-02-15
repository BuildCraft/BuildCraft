/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.utils.Utils;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class BlueprintDatabase {
	private final int bufferSize = 8192;
	private final String fileExt = ".bpt";
	private File blueprintFolder;

	private Set <BlueprintId> blueprintIds = new TreeSet<BlueprintId> ();

	//private Map<BlueprintId, BlueprintMeta> blueprintMetas = new HashMap<BlueprintId, BlueprintMeta>();
	private Map<BlueprintId, Blueprint> loadedBlueprints = new WeakHashMap<BlueprintId, Blueprint>();

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

		loadIndex(); // TODO: load index in a thread
	}

	/**
	 * Return a list of blueprint on a given page.
	 *
	 * FIXME: This returns blueprints in no particular order. We probably want
	 * to have an ordered list of blueprint instead
	 */
	public List <BlueprintId> getPage (int pageId, int pageSize) {
		List <BlueprintId> result = new ArrayList<BlueprintId>();

		int start = pageId * pageSize;
		int stop = (pageId + 1) * pageSize;

		int i = 0;

		for (BlueprintId id : blueprintIds) {
			i++;

			if (i >= stop) {
				break;
			}

			if (i >= start) {
				result.add (id);
			}
		}

		return result;
	}

	/**
	 * Get a specific blueprint by id.
	 *
	 * @note The blueprint will be loaded as needed.
	 *
	 * @param id blueprint id
	 * @return blueprint or null if it can't be retrieved
	 */
	public Blueprint get(BlueprintId id) {
		Blueprint ret = loadedBlueprints.get(id);

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
	public BlueprintId add(Blueprint blueprint) {
		BlueprintId id = save(blueprint);

		if (!blueprintIds.contains(id)) {
			blueprintIds.add(id);
		}

		if (!loadedBlueprints.containsKey(id)) {
			loadedBlueprints.put(id, blueprint);
		}

		return id;
	}

	private BlueprintId save(Blueprint blueprint) {
		NBTTagCompound nbt = new NBTTagCompound();
		blueprint.writeToNBT(nbt);
		
		ByteBuf buf = Unpooled.buffer();

		Utils.writeNBT(buf, nbt);

		byte[] data = new byte [buf.readableBytes()];
		buf.readBytes(data);

		blueprint.generateId(data);

		BlueprintId id = blueprint.meta.id;

		File blueprintFile = new File(blueprintFolder, String.format(Locale.ENGLISH, "%s" + fileExt, id.toString()));

		if (!blueprintFile.exists()) {
			OutputStream gzOs = null;
			try {
				gzOs = new GZIPOutputStream(new FileOutputStream(blueprintFile));

				gzOs.write(data);

				CompressedStreamTools.write(nbt, blueprintFile);
			} catch (IOException ex) {
				Logger.getLogger("Buildcraft").log(Level.SEVERE, String.format("Failed to save Blueprint file: %s %s", blueprintFile.getName(), ex.getMessage()));
			} finally {
				try {
					if (gzOs != null) gzOs.close();
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
			id.uniqueId = BlueprintId.toBytes (suffix);

			if (!blueprintIds.contains(id)) {
				blueprintIds.add(id);
			}
		}
	}

	private Blueprint load(final BlueprintId id) {
		/*FilenameFilter filter = new FilenameFilter() {
			String prefix = meta.getId().toString();

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(fileExt) && name.startsWith(prefix);
			}
		};

		for (File blueprintFile : blueprintFolder.listFiles(filter)) {
			RawBlueprint rawBlueprint = load(blueprintFile);

			if (rawBlueprint == null) {
				continue;
			}

			Blueprint blueprint;

			try {
				blueprint = new Blueprint(meta, rawBlueprint.nbt);
			} catch (Exception e) {
				// TODO: delete?
				continue;
			}

			loadedBlueprints.put(blueprint.getId(), blueprint);

			return blueprint;
		}*/

		return null;
	}
}
