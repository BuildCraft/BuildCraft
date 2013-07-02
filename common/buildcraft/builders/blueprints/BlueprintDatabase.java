/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlueprintDatabase {

	public static File configFolder;
	private static Map<UUID, Blueprint> blueprints = new HashMap<UUID, Blueprint>();

	public static Blueprint getBlueprint(UUID uuid) {
		Blueprint blueprint = blueprints.get(uuid);
		if (blueprint == null) {
			blueprint = loadBlueprint(uuid);
			addBlueprint(blueprint);
		}
		return blueprint;
	}

	public static void addBlueprint(Blueprint blueprint) {
		if (blueprint == null)
			return;
		blueprints.put(blueprint.getUUID(), blueprint);
	}

	private static File getBlueprintFolder() {
		File blueprintFolder = new File(configFolder, "buildcraft/blueprints/");
		if (!blueprintFolder.exists()) {
			blueprintFolder.mkdirs();
		}
		return blueprintFolder;
	}

	private static String uuidToString(UUID uuid) {
		return String.format(Locale.ENGLISH, "%x%x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
	}

	public static void saveBlueprint(Blueprint blueprint) {
		NBTTagCompound nbt = new NBTTagCompound();
		blueprint.writeToNBT(nbt);

		File blueprintFile = new File(getBlueprintFolder(), String.format(Locale.ENGLISH, "%x%x-%s.nbt", uuidToString(blueprint.getUUID()), blueprint.getName()));

		if (blueprintFile.exists())
			return;

		try {
			CompressedStreamTools.write(nbt, blueprintFile);
		} catch (IOException ex) {
			Logger.getLogger("Buildcraft").log(Level.SEVERE, String.format("Failed to save Blueprint file: %s %s", blueprintFile.getName(), ex.getMessage()));
		}
	}

	public static void saveBlueprints() {
		for (Blueprint blueprint : blueprints.values()) {
			saveBlueprint(blueprint);
		}
	}

	private static Blueprint loadBlueprint(final UUID uuid) {
		FilenameFilter filter = new FilenameFilter() {
			private String uuidString = uuidToString(uuid);

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(uuidString);
			}
		};

		NBTTagCompound nbt = null;
		File blueprintFolder = getBlueprintFolder();
		for (File blueprintFile : blueprintFolder.listFiles(filter)) {
			try {
				nbt = CompressedStreamTools.read(blueprintFile);
				break;
			} catch (IOException ex) {
				Logger.getLogger("Buildcraft").log(Level.SEVERE, String.format("Failed to load Blueprint file: %s %s", blueprintFile.getName(), ex.getMessage()));
			}
		}

		if (nbt == null) {
			return null;
		}
		return Blueprint.readFromNBT(nbt);
	}

	public static void loadBlueprints() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".nbt");
			}
		};
		File blueprintFolder = getBlueprintFolder();
		for (File blueprintFile : blueprintFolder.listFiles(filter)) {
			try {
				NBTTagCompound nbt = CompressedStreamTools.read(blueprintFile);
				addBlueprint(Blueprint.readFromNBT(nbt));
			} catch (IOException ex) {
				Logger.getLogger("Buildcraft").log(Level.SEVERE, String.format("Failed to load Blueprint file: %s %s", blueprintFile.getName(), ex.getMessage()));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void sendBlueprintsToServer() {
		// TODO
	}
}
