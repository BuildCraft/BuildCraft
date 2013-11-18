/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author Player
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlueprintDatabase {
	/**
	 * Initialize the blueprint database.
	 * 
	 * @param configDir config directory to read the blueprints from.
	 */
	public static void init(File configDir) {
		blueprintFolder = new File(new File(configDir, "buildcraft"), "blueprints");

		if (!blueprintFolder.exists()) blueprintFolder.mkdirs();

		loadIndex(); // TODO: load index in a thread
	}

	// TODO: server: send ids to the client on connect, mby full meta
	// TODO: client: send missing blueprints to the server after receiving the server's ids

	/**
	 * Get a list with the metadata for all available blueprints.
	 * 
	 * @return meta data iterable
	 */
	public static Iterable<BlueprintMeta> getList() {
		return blueprintMetas.values();
	}

	/**
	 * Get a specific blueprint by id.
	 * 
	 * @note The blueprint will be loaded as needed.
	 * 
	 * @param id blueprint id
	 * @return blueprint or null if it can't be retrieved
	 */
	public static Blueprint get(BlueprintId id) {
		Blueprint ret = blueprints.get(id);

		if (ret == null) {
			BlueprintMeta meta = blueprintMetas.get(id);
			if (meta == null) return null; // no meta -> no bpt as well

			ret = load(meta);
		}

		return ret;
	}

	/**
	 * Add a blueprint to the database and save it to disk.
	 * 
	 * @param blueprint blueprint to add
	 * @return id for the added blueprint
	 */
	public static BlueprintId add(Blueprint blueprint) {
		BlueprintId id = save(blueprint);

		blueprint.setId(id);

		BlueprintMeta prevValue = blueprintMetas.put(id, blueprint.getMeta());
		blueprints.put(id, blueprint);

		if (prevValue != null) {
			// TODO: duplicate entry, shouldn't happen
		}

		return id;
	}

	private static BlueprintId save(Blueprint blueprint) {
		NBTTagCompound nbt = new NBTTagCompound();
		blueprint.writeToNBT(nbt);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);

		try {
			NBTBase.writeNamedTag(nbt, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] data = bos.toByteArray();

		BlueprintId id = BlueprintId.generate(data);

		File blueprintFile = new File(blueprintFolder, String.format(Locale.ENGLISH, "%s-%s.nbt", id.toString(), blueprint.getName()));

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

	private static void loadIndex() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(fileExt);
			}
		};

		for (File blueprintFile : blueprintFolder.listFiles(filter)) {
			RawBlueprint rawBlueprint = load(blueprintFile);

			if (rawBlueprint == null) {
				// TODO: delete?
				continue;
			}

			BlueprintMeta meta;

			try {
				meta = new BlueprintMeta(rawBlueprint.id, rawBlueprint.nbt);
			} catch (Exception e) {
				// TODO: delete?
				continue;
			}

			// TODO: check if the filename is matching id+name

			BlueprintMeta prevValue = blueprintMetas.put(meta.getId(), meta);

			if (prevValue != null) {
				// TODO: duplicate entry, handle
			}
		}
	}

	private static Blueprint load(final BlueprintMeta meta) {
		FilenameFilter filter = new FilenameFilter() {
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

			blueprints.put(blueprint.getId(), blueprint);

			return blueprint;
		}

		return null;
	}

	private static RawBlueprint load(File file) {
		InputStream fileIs = null;
		ByteArrayOutputStream decompressedStream;

		try {
			fileIs = new GZIPInputStream(new FileInputStream(file), bufferSize);
			decompressedStream = new ByteArrayOutputStream(bufferSize * 4);
			byte buffer[] = new byte[bufferSize];
			int len;

			while ((len = fileIs.read(buffer)) != -1) {
				decompressedStream.write(buffer, 0, len);
			}
		} catch (IOException e) {
			Logger.getLogger("Buildcraft").log(Level.SEVERE, String.format("Failed to load Blueprint file: %s %s", file.getName(), e.getMessage()));
			return null;
		} finally {
			try {
				fileIs.close();
			} catch (IOException e) {}
		}

		byte[] data = decompressedStream.toByteArray();
		BlueprintId id = BlueprintId.generate(data);

		DataInputStream dataIs = new DataInputStream(new ByteArrayInputStream(data));
		NBTTagCompound nbt;

		try {
			nbt = CompressedStreamTools.read(dataIs);
		} catch (IOException e) {
			return null;
		}

		return new RawBlueprint(id, nbt);
	}

	private static class RawBlueprint {
		RawBlueprint(BlueprintId id, NBTTagCompound nbt) {
			this.id = id;
			this.nbt = nbt;
		}

		final BlueprintId id;
		final NBTTagCompound nbt;
	}

	private static final int bufferSize = 8192;
	private static final String fileExt = ".bpt";
	private static File blueprintFolder;
	private static Map<BlueprintId, BlueprintMeta> blueprintMetas = new HashMap<BlueprintId, BlueprintMeta>();
	private static Map<BlueprintId, Blueprint> blueprints = new WeakHashMap<BlueprintId, Blueprint>();
}
