package net.minecraft.src.buildcraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.forge.Configuration;

public class BuildCraftConfiguration extends Configuration {

	public BuildCraftConfiguration(File file, boolean loadLegacy) {
		super(file);
		
		if (loadLegacy) {
			loadLegacyProperties();
		}
	}

	public void loadLegacyProperties () {
		File cfgfile = CoreProxy.getPropertyFile();
		Properties props = new Properties();
		
		try {			
			if (!cfgfile.exists()) {
				return;
			}
			if (cfgfile.canRead()) {
				FileInputStream fileinputstream = new FileInputStream(cfgfile);
				props.load(fileinputstream);
				fileinputstream.close();
			}

			getOrCreateProperty("stonePipe.id", PropertyKind.Block,
					props.getProperty("stonePipe.blockId"));
			getOrCreateProperty("woodenPipe.id", PropertyKind.Block,
					props.getProperty("woodenPipe.blockId"));
			getOrCreateProperty("ironPipe.id", PropertyKind.Block,
					props.getProperty("ironPipe.blockId"));
			getOrCreateProperty("goldenPipe.id", PropertyKind.Block,
					props.getProperty("goldenPipe.blockId"));
			getOrCreateProperty("diamondPipe.id", PropertyKind.Block,
					props.getProperty("diamondPipe.blockId"));
			getOrCreateProperty("obsidianPipe.id", PropertyKind.Block,
					props.getProperty("obsidianPipeBlock.blockId"));
			getOrCreateProperty("autoWorkbench.id", PropertyKind.Block,
					props.getProperty("autoWorkbench.blockId"));
			getOrCreateProperty("miningWell.id", PropertyKind.Block,
					props.getProperty("miningWell.blockId"));
			getOrCreateProperty("quarry.id", PropertyKind.Block,
					props.getProperty("quarry.blockId"));
			getOrCreateProperty("drill.id", PropertyKind.Block,
					props.getProperty("drill.blockId"));
			getOrCreateProperty("frame.id", PropertyKind.Block,
					props.getProperty("frame.blockId"));
			getOrCreateProperty("marker.id", PropertyKind.Block,
					props.getProperty("marker.blockId"));
			getOrCreateProperty("filler.id", PropertyKind.Block,
					props.getProperty("filler.blockId"));
			
			getOrCreateProperty("woodenGearItem.id", PropertyKind.Item,
					props.getProperty("woodenGearItem.id"));
			getOrCreateProperty("stoneGearItem.id", PropertyKind.Item,
					props.getProperty("stoneGearItem.id"));
			getOrCreateProperty("ironGearItem.id", PropertyKind.Item,
					props.getProperty("ironGearItem.id"));
			getOrCreateProperty("goldenGearItem.id", PropertyKind.Item,
					props.getProperty("goldGearItem.id"));
			getOrCreateProperty("diamondGearItem.id", PropertyKind.Item,
					props.getProperty("diamondGearItem.id"));

			getOrCreateProperty("mining.enabled", PropertyKind.General,
					props.getProperty("mining.enabled"));
			getOrCreateProperty("current.continuous", PropertyKind.General,
					props.getProperty("current.continous"));
			
			cfgfile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save () {
		Property versionProp = null;
		
		if (!generalProperties.containsKey("version")) {
			versionProp = new Property();
			versionProp.name = "version";				
			generalProperties.put("version", versionProp);
		} else {
			versionProp = generalProperties.get("version");
		}
		
		versionProp.value = mod_BuildCraftCore.version();	
		
		super.save();
	}
	
}
