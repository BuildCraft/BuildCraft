package buildcraft.builders.json;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;

import buildcraft.api.core.BCLog;
import buildcraft.core.blueprints.SchematicRegistry;

public final class BuilderSupportLoader {
	public static final BuilderSupportLoader INSTANCE = new BuilderSupportLoader();
	private final Set<String> checkedModIds = new HashSet<String>();

	private BuilderSupportLoader() {

	}

	private void register(BuilderSupportFile supportFile, BuilderSupportEntry e, String modId, String name, Set<Block> loadedBlocks) {
		Block b = Block.getBlockFromName(modId + ":" + name);

		for (int i = 0; i < 16; i++) {
			if (!SchematicRegistry.INSTANCE.isSupported(b, i) && e.isValidForMeta(i) && supportFile.isValidForMeta(i)) {
				SchematicRegistry.INSTANCE.registerSchematicBlock(b, i, SchematicJSON.class, supportFile, name);
				loadedBlocks.add(b);
			}
		}
	}

	private void processJSONFile(String filename, ClassLoader classLoader, String modId, String blockName) {
		try {
			BuilderSupportFile supportFile = new BuilderSupportFile(filename, classLoader, blockName);
			Set<Block> loadedBlocks = new HashSet<Block>();

			for (BuilderSupportEntry e : supportFile.entries) {
				if (e.names == null) {
					register(supportFile, e, modId, e.name, loadedBlocks);
				} else {
					for (String name : e.names) {
						register(supportFile, e, modId, name, loadedBlocks);
					}
				}
			}

			if (loadedBlocks.size() > 0) {
				BCLog.logger.info("Loaded " + loadedBlocks.size() + " block" + (loadedBlocks.size() == 1 ? "" : "s") +  " from Builder support definition " + filename);
			}
		} catch (JSONValidationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			if (!e.getMessage().equals(filename)) {
				e.printStackTrace();
			}
		}
	}

	public void init() {
		for (String blockFullName : (Collection<String>) Block.blockRegistry.getKeys()) {
			Block b = (Block) Block.blockRegistry.getObject(blockFullName);
			if (b == null) {
				continue;
			}

			boolean noSupport = false;

			for (int i = 0; i < 16; i++) {
				if (!SchematicRegistry.INSTANCE.isSupported(b, i)) {
					noSupport = true;
					break;
				}
			}

			if (!noSupport) {
				continue;
			}

			String modId, pathDir, blockName, pathName;
			ClassLoader classLoader = b.getClass().getClassLoader();

			if (blockFullName.contains(":")) {
				modId = blockFullName.substring(0, blockFullName.indexOf(":"));
				pathDir = modId.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
				blockName = blockFullName.substring(blockFullName.indexOf(":") + 1);
				pathName = blockName;
				if (pathDir.equals("minecraft")) {
					pathDir = "buildcraftbuilders";
					pathName = "vanilla/" + blockName;
				}
			} else {
				modId = "minecraft";
				pathDir = "buildcraftbuilders";
				blockName = blockFullName;
				pathName = "unknown/" + blockFullName;
			}

			if (!checkedModIds.contains(modId)) {
				checkedModIds.add(modId);
				if (modId.equals("minecraft")) {
					processJSONFile("assets/" + pathDir + "/bcbuilder/vanillaBlocks.json", classLoader, modId, null);
				} else {
					processJSONFile("assets/buildcraftcompat/bcbuilder/" + pathDir + "/blocks.json", classLoader, modId, null);
					processJSONFile("assets/" + pathDir + "/bcbuilder/blocks.json", classLoader, modId, null);
					processJSONFile("bcbuilder/" + pathDir + "/blocks/blocks.json", classLoader, modId, blockName);
				}
			}

			processJSONFile("assets/" + pathDir + "/bcbuilder/blocks/" + pathName + ".json", classLoader, modId, blockName);
			processJSONFile("assets/buildcraftcompat/bcbuilder/" + pathDir + "/blocks/" + pathName + ".json", classLoader, modId, blockName);
			processJSONFile("bcbuilder/" + pathDir + "/blocks/" + pathName + ".json", classLoader, modId, blockName);
		}
	}
}
