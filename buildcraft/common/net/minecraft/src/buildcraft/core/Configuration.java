package net.minecraft.src.buildcraft.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

	public Configuration (File file, boolean loadLegacy) {
		this.file = file;
		
		if (loadLegacy) {
			loadLegacyProperties();
		}
		
		load ();
	}
	
	public Property getOrCreateBlockIdProperty (String key, int defaultId) {
		if (blockProperties.containsKey(key)) {			
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
		if (blockProperties.containsKey(key)) {
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
		if (itemProperties.containsKey(key)) {
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
		if (generalProperties.containsKey(key)) {
			return generalProperties.get(key);
		} else {
			Property property = new Property();

			generalProperties.put(key, property);
			property.name = key;
			
			property.value = defaultValue;
    		return property;    		
		}
	}
	
	public void load () {
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile()) {
				return;
			}
			
			if (file.canRead()) {
				FileInputStream fileinputstream = new FileInputStream(
						file);
				
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(fileinputstream, "8859_1"));
				
				String line;
				TreeMap<String, Property> currentMap = null;
				
				while (true) {
					line = buffer.readLine();
					
					if (line == null) {
						break;
					}
					
					int nameStart = -1, nameEnd = -1;
					boolean skip = false;
					
					for (int i = 0; i < line.length() && !skip; ++i) {
						if (Character.isLetterOrDigit(line.charAt(i))
								|| line.charAt(i) == '.') {
							if (nameStart == -1) {
								nameStart = i;
							}
							
							nameEnd = i;
						} else if (Character.isWhitespace(line.charAt(i))) {
							// ignore space charaters
						} else {
							switch (line.charAt(i)) {
							case '#':
								skip = true;
								continue;
							case '{':
								String scopeName = line
										.substring(nameStart, nameEnd + 1);
								
								if (scopeName.equals("general")) {
									currentMap = generalProperties;
								} else if (scopeName.equals("block")) {
									currentMap = blockProperties;
								} else if (scopeName.equals("item")) {
									currentMap = itemProperties; 
								} else {
									throw new RuntimeException("unknown section "
											+ scopeName);
								}
								
								break;
							case '}':
								currentMap = null;
								break;
							case '=':
								String propertyName = line
										.substring(nameStart, nameEnd + 1);
								
								if (currentMap == null) {
									throw new RuntimeException("property "
											+ propertyName + " has no scope");						
								}
								
								Property prop = new Property();
								prop.name = propertyName;
								prop.value = line.substring(i + 1);
								
								currentMap.put(propertyName, prop);
								
								break;
							default:
								throw new RuntimeException("unknown character "
										+ line.charAt(i));								
							}
						}
					}
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			
			Property versionProp = null;
			
			if (!generalProperties.containsKey("version")) {
				versionProp = new Property();
				versionProp.name = "version";				
				generalProperties.put("version", versionProp);
			} else {
				versionProp = generalProperties.get("version");
			}
			
			versionProp.value = mod_BuildCraftCore.version();	
			
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
			System.out.println ("LOAD WOODEN PIPE");
			System.out.println (getOrCreateBlockProperty("woodenPipe.id",
					props.getProperty("woodenPipe.blockId")).value);
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
			
			cfgfile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
