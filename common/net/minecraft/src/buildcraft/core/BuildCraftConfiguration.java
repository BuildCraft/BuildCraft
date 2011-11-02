/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.Property;

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

			getOrCreateProperty("stonePipe.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("stonePipe.blockId"));
			getOrCreateProperty("woodenPipe.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("woodenPipe.blockId"));
			getOrCreateProperty("ironPipe.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("ironPipe.blockId"));
			getOrCreateProperty("goldenPipe.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("goldenPipe.blockId"));
			getOrCreateProperty("diamondPipe.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("diamondPipe.blockId"));
			getOrCreateProperty("obsidianPipe.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("obsidianPipeBlock.blockId"));
			getOrCreateProperty("autoWorkbench.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("autoWorkbench.blockId"));
			getOrCreateProperty("miningWell.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("miningWell.blockId"));
			getOrCreateProperty("quarry.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("quarry.blockId"));
			getOrCreateProperty("drill.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("drill.blockId"));
			getOrCreateProperty("frame.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("frame.blockId"));
			getOrCreateProperty("marker.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("marker.blockId"));
			getOrCreateProperty("filler.id", Configuration.BLOCK_PROPERTY,
					props.getProperty("filler.blockId"));
			
			getOrCreateProperty("woodenGearItem.id", Configuration.ITEM_PROPERTY,
					props.getProperty("woodenGearItem.id"));
			getOrCreateProperty("stoneGearItem.id", Configuration.ITEM_PROPERTY,
					props.getProperty("stoneGearItem.id"));
			getOrCreateProperty("ironGearItem.id", Configuration.ITEM_PROPERTY,
					props.getProperty("ironGearItem.id"));
			getOrCreateProperty("goldenGearItem.id", Configuration.ITEM_PROPERTY,
					props.getProperty("goldGearItem.id"));
			getOrCreateProperty("diamondGearItem.id", Configuration.ITEM_PROPERTY,
					props.getProperty("diamondGearItem.id"));

			getOrCreateProperty("mining.enabled", Configuration.GENERAL_PROPERTY,
					props.getProperty("mining.enabled"));
			getOrCreateProperty("current.continuous", Configuration.GENERAL_PROPERTY,
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
