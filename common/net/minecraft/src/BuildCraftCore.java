/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src;

import java.io.File;
import java.util.TreeMap;

import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.LiquidData;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.BuildCraftConfiguration;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.ItemBuildCraftTexture;
import net.minecraft.src.buildcraft.core.RedstonePowerFramework;
import net.minecraft.src.buildcraft.core.network.ConnectionHandler;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

public class BuildCraftCore {
	
	private static EntityPlayer buildCraftPlayer;
	
	public static boolean debugMode = false;
	public static boolean modifyWorld = false;
	public static boolean trackNetworkUsage = false;
	
	public static int updateFactor = 10;
	
	public static BuildCraftConfiguration mainConfiguration;
	
	public static TreeMap<BlockIndex, PacketUpdate> bufferedDescriptions = new TreeMap<BlockIndex, PacketUpdate>();
	
	public static final int trackedPassiveEntityId = 156;
	
	public static boolean continuousCurrentModel;
	
	private static boolean initialized = false;
	private static boolean gearsInitialized = false;
	
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;
	public static Item wrenchItem;
	
	public static int redLaserTexture;
	public static int blueLaserTexture;
	public static int stripesLaserTexture;
	public static int transparentTexture;
	
	public static int blockByEntityModel;
	public static int pipeModel;
	public static int markerModel;
	public static int oilModel;
	
	public static String customBuildCraftTexture =
		"/net/minecraft/src/buildcraft/core/gui/block_textures.png";
	
	public static String customBuildCraftSprites =
		"/net/minecraft/src/buildcraft/core/gui/item_textures.png";
	
	public static int refineryInput = 0;
	
	public static boolean loadDefaultRecipes = true;
	public static boolean forcePneumaticPower = false;
	public static boolean consumeWaterSources = true;
	
	public static void load() {
		// Register connection handler
		MinecraftForge.registerConnectionHandler(new ConnectionHandler());
	}

	@SuppressWarnings({ "all" })
	public static void initialize () {
		if (initialized) {
			return;
		}
		
		ModLoader.getLogger().fine ("Starting BuildCraft " + mod_BuildCraftCore.version());
		ModLoader.getLogger().fine ("Copyright (c) SpaceToad, 2011");
		ModLoader.getLogger().fine ("http://www.mod-buildcraft.com");
		
		System.out.println ("Starting BuildCraft " + mod_BuildCraftCore.version());
		System.out.println ("Copyright (c) SpaceToad, 2011");
		System.out.println ("http://www.mod-buildcraft.com");		
		
		initialized = true;
				
		mainConfiguration = new BuildCraftConfiguration(new File(
				CoreProxy.getBuildCraftBase(), "config/buildcraft.cfg"), true);
		mainConfiguration.load();
		
		redLaserTexture = 0 * 16 + 2;
		blueLaserTexture = 0 * 16 + 1;
		stripesLaserTexture = 0 * 16 + 3;
		transparentTexture = 0 * 16 + 0;
		
		Property continuousCurrent = BuildCraftCore.mainConfiguration
				.getOrCreateBooleanProperty("current.continuous",
						Configuration.CATEGORY_GENERAL,
						DefaultProps.CURRENT_CONTINUOUS);
		continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";

		continuousCurrentModel = Boolean.parseBoolean(continuousCurrent.value);
		
		Property trackNetwork = BuildCraftCore.mainConfiguration
		.getOrCreateBooleanProperty("trackNetworkUsage",
				Configuration.CATEGORY_GENERAL,
				false);
		
		trackNetworkUsage = Boolean.parseBoolean(trackNetwork.value);
		
		Property powerFrameworkClass = BuildCraftCore.mainConfiguration
				.getOrCreateProperty("power.framework",
						Configuration.CATEGORY_GENERAL,
						"buildcraft.energy.PneumaticPowerFramework");
		
		Property factor = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("network.updateFactor",
				Configuration.CATEGORY_GENERAL, 10);
		factor.comment = 
			"increasing this number will decrease network update frequency, useful for overloaded servers";

		updateFactor = Integer.parseInt(factor.value);
		
		String prefix = "";
		
		if (BuildCraftCore.class.getName().startsWith("net.minecraft.src.")) {
			prefix = "net.minecraft.src.";
		}
		
		if (forcePneumaticPower) {
			try {
				PowerFramework.currentFramework = (PowerFramework) Class
						.forName(prefix + "buildcraft.energy.PneumaticPowerFramework")
						.getConstructor().newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			try {
				String className = powerFrameworkClass.value;
				if (className.startsWith("net.minecraft.src.")) {
					className = className.replace("net.minecraft.src.", "");
				}
				
				PowerFramework.currentFramework = (PowerFramework) Class
				.forName(prefix + className).getConstructor()
				.newInstance();
			} catch (Throwable e) {
				e.printStackTrace();
				PowerFramework.currentFramework = new RedstonePowerFramework();
			}
		}
		
		Property wrenchId = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("wrench.id",
				Configuration.CATEGORY_ITEM, DefaultProps.WRENCH_ID);
		
		mainConfiguration.save();
		
		initializeGears ();
		
		wrenchItem = (new ItemBuildCraftTexture(Integer.parseInt(wrenchId.value)))
		.setIconIndex(0 * 16 + 2)
		.setItemName("wrenchItem");		
		CoreProxy.addName(wrenchItem, "Wrench");
		
		API.liquids.add(new LiquidData(Block.waterStill.blockID,
				Item.bucketWater.shiftedIndex));
		API.liquids.add(new LiquidData(Block.lavaStill.blockID, 
				Item.bucketLava.shiftedIndex));
		
		API.softBlocks [Block.waterMoving.blockID] = true;
		API.softBlocks [Block.waterStill.blockID] = true;

		mainConfiguration.save();
		
		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
	}
	
	public static void loadRecipes () {
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		craftingmanager.addRecipe(new ItemStack(wrenchItem), new Object[] {
			"I I", " G ", " I ", Character.valueOf('I'), Item.ingotIron,
			Character.valueOf('G'), stoneGearItem });
		
		craftingmanager.addRecipe(new ItemStack(woodenGearItem), new Object[] {
			" S ", "S S", " S ", Character.valueOf('S'), Item.stick});
		
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
			Character.valueOf('G'), woodenGearItem });
		
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
			Character.valueOf('G'), stoneGearItem });
		
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
			Character.valueOf('G'), ironGearItem });
		
		craftingmanager.addRecipe(new ItemStack(diamondGearItem), new Object[] {
			" I ", "IGI", " I ", Character.valueOf('I'), Item.diamond,
			Character.valueOf('G'), goldGearItem });
	}
	
	public static void initializeGears () {
		if (gearsInitialized) {
			return;
		}
		
		Property woodenGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("woodenGearItem.id",
						Configuration.CATEGORY_ITEM,
						DefaultProps.WOODEN_GEAR_ID);
		Property stoneGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("stoneGearItem.id",
						Configuration.CATEGORY_ITEM, DefaultProps.STONE_GEAR_ID);
		Property ironGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("ironGearItem.id",
						Configuration.CATEGORY_ITEM, DefaultProps.IRON_GEAR_ID);
		Property goldenGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("goldenGearItem.id",
						Configuration.CATEGORY_ITEM,
						DefaultProps.GOLDEN_GEAR_ID);
		Property diamondGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("diamondGearItem.id",
						Configuration.CATEGORY_ITEM,
						DefaultProps.DIAMOND_GEAR_ID);
		Property modifyWorld = BuildCraftCore.mainConfiguration
				.getOrCreateBooleanProperty("modifyWorld",
						Configuration.CATEGORY_GENERAL, true);
		modifyWorld.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";
		
		BuildCraftCore.mainConfiguration.save();
		
		BuildCraftCore.modifyWorld = modifyWorld.value.equals("true");
		
		gearsInitialized = true;
		
		woodenGearItem = (new ItemBuildCraftTexture(Integer.parseInt(woodenGearId.value)))
				.setIconIndex(1 * 16 + 0)
				.setItemName("woodenGearItem");
		CoreProxy.addName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new ItemBuildCraftTexture(Integer.parseInt(stoneGearId.value)))
				.setIconIndex(1 * 16 + 1)
				.setItemName("stoneGearItem");		
		CoreProxy.addName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new ItemBuildCraftTexture(Integer.parseInt(ironGearId.value)))
				.setIconIndex(1 * 16 + 2)
				.setItemName("ironGearItem");
		CoreProxy.addName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new ItemBuildCraftTexture(Integer.parseInt(goldenGearId.value)))
				.setIconIndex(1 * 16 + 3)
				.setItemName("goldGearItem");
		CoreProxy.addName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new ItemBuildCraftTexture(Integer.parseInt(diamondGearId.value)))
				.setIconIndex(1 * 16 + 4)
				.setItemName("diamondGearItem");
		CoreProxy.addName(diamondGearItem, "Diamond Gear");

		BuildCraftCore.mainConfiguration.save();
	}
	
	
	public static void initializeModel (BaseMod mod) {
		 blockByEntityModel = ModLoader.getUniqueBlockModelID(mod, true);
		 pipeModel = ModLoader.getUniqueBlockModelID(mod, true);
		 markerModel = ModLoader.getUniqueBlockModelID(mod, false);
		 oilModel = ModLoader.getUniqueBlockModelID(mod, false);
	}
	
	public static EntityPlayer getBuildCraftPlayer (World world) {
		if (buildCraftPlayer == null) {
			buildCraftPlayer = new EntityPlayer(world) {
				
				public void func_6420_o() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		
		return buildCraftPlayer;
	}
}
