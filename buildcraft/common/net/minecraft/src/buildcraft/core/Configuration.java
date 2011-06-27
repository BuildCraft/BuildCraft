package net.minecraft.src.buildcraft.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

import net.minecraft.src.Block;
import net.minecraft.src.mod_BuildCraftCore;

public class Configuration {
	
	File file;
	
	public static class Property {
		public String name;
		public String value;
		public String comment;		
	}
	
	public TreeMap<String, Property> blockProperties = new TreeMap<String, Property>();
	public TreeMap<String, Property> itemProperties = new TreeMap<String, Property>();
	public TreeMap<String, Property> generalProperties = new TreeMap<String, Property>();

	public Configuration (File file) {
		this.file = file;
		
		// implement load + load from legacy
		// Override conditions: mods and world names
		// If there is an old BuildCraft.cfg, use the values and then delete it.
	}
	
	public Property getOrCreateBlockIdProperty (String key, int defaultId) {
		if (blockProperties.containsValue(key)) {
			return blockProperties.get(key);
		} else {
			Property property = new Property();

			blockProperties.put(key, property);
			property.name = key;
			
			if (Block.blocksList [defaultId] == null) {
				property.value = Integer.toString(defaultId);
				return property;
			} else {
    			for (int j = Block.blocksList.length - 1; j >= 0; --j) {
    				if (Block.blocksList [j] == null) {
    					property.value = Integer.toString(j);
    					return property;
    				}
    			}
    			
				throw new RuntimeException("No more block ids available for "
						+ key);
			}
		}
	}
	
	public Property getOrCreateBlockProperty (String key, String defaultValue) {
		if (blockProperties.containsValue(key)) {
			return blockProperties.get(key);
		} else {
			Property property = new Property();

			blockProperties.put(key, property);
			property.name = key;
			
			property.value = defaultValue;
    		return property;    		
		}
	}
	
	public Property getOrCreateItemProperty (String key, String defaultValue) {
		if (itemProperties.containsValue(key)) {
			return itemProperties.get(key);
		} else {
			Property property = new Property();

			itemProperties.put(key, property);
			property.name = key;
			
			property.value = defaultValue;
    		return property;    		
		}
	}
	
	public Property getOrCreateGeneralProperty (String key, String defaultValue) {
		if (generalProperties.containsValue(key)) {
			return generalProperties.get(key);
		} else {
			Property property = new Property();

			generalProperties.put(key, property);
			property.name = key;
			
			property.value = defaultValue;
    		return property;    		
		}
	}
	
	public void save () {
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			
			if (!file.exists() && !file.createNewFile()) {
				return;
			}
			if (file.canWrite()) {
				FileOutputStream fileoutputstream = new FileOutputStream(
						file);
				
				BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fileoutputstream, "8859_1"));
				
				buffer.write("# BuildCraft configuration file\n");
				buffer.write("# Generated on "
						+ DateFormat.getInstance().format(new Date()) + "\n");
				buffer.write("\n");
				buffer.write("###########\n");
				buffer.write("# General #\n");
				buffer.write("###########\n\n");
				
				buffer.write("general {\n");
				buffer.write("   version=" + mod_BuildCraftCore.version() + "\n");
				writeProperties(buffer, generalProperties.values());
				buffer.write("}\n\n");
				
				buffer.write("#########\n");
				buffer.write("# Block #\n");
				buffer.write("#########\n\n");				
				
				buffer.write("block {\n");
				writeProperties(buffer, blockProperties.values());
				buffer.write("}\n\n");
				
				buffer.write("########\n");
				buffer.write("# Item #\n");
				buffer.write("########\n\n");		
															
				buffer.write("item {\n");
				writeProperties(buffer, itemProperties.values());
				buffer.write("}\n\n");
				
				buffer.close();				
				fileoutputstream.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeProperties(BufferedWriter buffer,
			Collection<Property> props) throws IOException {
		for (Property property : props) {
			if (property.comment != null) {
				buffer.write("   # " + property.comment + "\n");
			}

			buffer.write("   " + property.name + "=" + property.value);
			buffer.write("\n");
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

			getOrCreateBlockProperty("stonePipe.id",
					props.getProperty("stonePipe.blockId"));
			getOrCreateBlockProperty("woodenPipe.id",
					props.getProperty("woodenPipe.blockId"));
			getOrCreateBlockProperty("ironPipe.id",
					props.getProperty("ironPipe.blockId"));
			getOrCreateBlockProperty("goldenPipe.id",
					props.getProperty("goldenPipe.blockId"));
			getOrCreateBlockProperty("diamondPipe.id",
					props.getProperty("diamondPipe.blockId"));
			getOrCreateBlockProperty("obsidianPipe.id",
					props.getProperty("obsidianPipeBlock.blockId"));
			getOrCreateBlockProperty("autoWorkbench.id",
					props.getProperty("autoWorkbench.blockId"));
			getOrCreateBlockProperty("miningWell.id",
					props.getProperty("miningWell.blockId"));
			getOrCreateBlockProperty("quarry.id",
					props.getProperty("quarry.blockId"));
			getOrCreateBlockProperty("drill.id",
					props.getProperty("drill.blockId"));
			getOrCreateBlockProperty("frame.id",
					props.getProperty("frame.blockId"));
			getOrCreateBlockProperty("marker.id",
					props.getProperty("marker.blockId"));
			getOrCreateBlockProperty("filler.id",
					props.getProperty("filler.blockId"));
			
			getOrCreateItemProperty("woodenGearItem.id",
					props.getProperty("woodenGearItem.id"));
			getOrCreateItemProperty("stoneGearItem.id",
					props.getProperty("stoneGearItem.id"));
			getOrCreateItemProperty("ironGearItem.id",
					props.getProperty("ironGearItem.id"));
			getOrCreateItemProperty("goldenGearItem.id",
					props.getProperty("goldGearItem.id"));
			getOrCreateItemProperty("diamondGearItem.id",
					props.getProperty("diamondGearItem.id"));

			
			getOrCreateGeneralProperty("mining.enabled",
					props.getProperty("mining.enabled"));
			getOrCreateGeneralProperty("current.continuous",
					props.getProperty("current.continous"));
			
//			cheatBlock.blockId=255

			
//			cfgfile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
