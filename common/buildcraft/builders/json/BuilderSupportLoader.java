package buildcraft.builders.json;

import buildcraft.api.core.BCLog;
import buildcraft.core.blueprints.SchematicRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public final class BuilderSupportLoader {
	public static final BuilderSupportLoader INSTANCE = new BuilderSupportLoader();
	private final Set<String> checkedModIds = new HashSet<String>();

	private BuilderSupportLoader() {

	}

	private void register(BuilderSupportFile supportFile, BuilderSupportEntry e, String modId, String name, Set<Block> loadedBlocks) {
		Block b = Block.getBlockFromName(modId + ":" + name);

		for (IBlockState state : b.getBlockState().getValidStates()) {
			if (!SchematicRegistry.INSTANCE.isSupported(state) && e.isValidForState(state) && supportFile.isValidForState(state)) {
				SchematicRegistry.INSTANCE.registerSchematicBlock(state, SchematicJSON.class, supportFile, name);
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
		for (ResourceLocation blockLoc : Block.blockRegistry.getKeys()) {
			Block b = Block.blockRegistry.getObject(blockLoc);
			if (b == null) {
				continue;
			}

			boolean noSupport = false;

			for (IBlockState state : b.getBlockState().getValidStates()) {
				if (!SchematicRegistry.INSTANCE.isSupported(state)) {
					noSupport = true;
					break;
				}
			}

			if (!noSupport) {
				continue;
			}

			String modId, pathDir, blockName, pathName;
			ClassLoader classLoader = b.getClass().getClassLoader();

			if (blockLoc.getResourceDomain().length() > 0) {
				modId = blockLoc.getResourceDomain();
				pathDir = modId.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
				blockName = blockLoc.getResourcePath();
				pathName = blockName;
				if (pathDir.equals("minecraft")) {
					pathDir = "buildcraftbuilders";
					pathName = "vanilla/" + blockName;
				}
			} else {
				modId = "minecraft";
				pathDir = "buildcraftbuilders";
				blockName = blockLoc.getResourcePath();
				pathName = "unknown/" + blockLoc.getResourcePath();
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
