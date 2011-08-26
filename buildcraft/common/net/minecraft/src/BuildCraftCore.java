package net.minecraft.src;

import java.io.File;
import java.util.TreeMap;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.BuildCraftConfiguration;
import net.minecraft.src.buildcraft.core.BuildCraftItem;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.RedstonePowerFramework;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.Property;

public class BuildCraftCore {
	
	public static BuildCraftConfiguration mainConfiguration;
	
	public static TreeMap<BlockIndex, Packet230ModLoader> bufferedDescriptions = new TreeMap<BlockIndex, Packet230ModLoader>();
	
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
	
	public static PowerFramework powerFramework;
	
	public static final int OIL_BUCKET_QUANTITY = 1000;
	
	@SuppressWarnings({ "all" })
	public static void initialize () {
		if (initialized) {
			return;
		}
		
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
						Configuration.GENERAL_PROPERTY,
						DefaultProps.CURRENT_CONTINUOUS);
		continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";

		continuousCurrentModel = Boolean.parseBoolean(continuousCurrent.value);
		
		Property powerFrameworkClass = BuildCraftCore.mainConfiguration
				.getOrCreateProperty("power.framework",
						Configuration.GENERAL_PROPERTY,
						"buildcraft.energy.PneumaticPowerFramework");
		
		try {
			powerFramework = (PowerFramework) Class
					.forName(powerFrameworkClass.value).getConstructor(null)
					.newInstance(null);
		} catch (Throwable e) {
			e.printStackTrace();
			powerFramework = new RedstonePowerFramework();
		}
		
		Property wrenchId = BuildCraftCore.mainConfiguration
		.getOrCreateIntProperty("wrench.id",
				Configuration.ITEM_PROPERTY, DefaultProps.WRENCH_ID);
		
		mainConfiguration.save();
		
		initializeGears ();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		wrenchItem = (new BuildCraftItem(Integer.parseInt(wrenchId.value)))
		.setIconIndex(0 * 16 + 2)
		.setItemName("wrenchItem");
		craftingmanager.addRecipe(new ItemStack(wrenchItem), new Object[] {
				"I I", " G ", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		CoreProxy.addName(wrenchItem, "Wrench");
		
		mainConfiguration.save();
	}
	
	public static void initializeGears () {
		if (gearsInitialized) {
			return;
		}
		
		Property woodenGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("woodenGearItem.id",
						Configuration.ITEM_PROPERTY,
						DefaultProps.WOODEN_GEAR_ID);
		Property stoneGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("stoneGearItem.id",
						Configuration.ITEM_PROPERTY, DefaultProps.STONE_GEAR_ID);
		Property ironGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("ironGearItem.id",
						Configuration.ITEM_PROPERTY, DefaultProps.IRON_GEAR_ID);
		Property goldenGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("goldenGearItem.id",
						Configuration.ITEM_PROPERTY,
						DefaultProps.GOLDEN_GEAR_ID);
		Property diamondGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("diamondGearItem.id",
						Configuration.ITEM_PROPERTY,
						DefaultProps.DIAMOND_GEAR_ID);
		
		BuildCraftCore.mainConfiguration.save();
		
		gearsInitialized = true;
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		woodenGearItem = (new BuildCraftItem(Integer.parseInt(woodenGearId.value)))
				.setIconIndex(1 * 16 + 0)
				.setItemName("woodenGearItem");
		craftingmanager.addRecipe(new ItemStack(woodenGearItem), new Object[] {
		" S ", "S S", " S ", Character.valueOf('S'), Item.stick});
		CoreProxy.addName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new BuildCraftItem(Integer.parseInt(stoneGearId.value)))
				.setIconIndex(1 * 16 + 1)
				.setItemName("stoneGearItem");
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
				Character.valueOf('G'), woodenGearItem });
		CoreProxy.addName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new BuildCraftItem(Integer.parseInt(ironGearId.value)))
				.setIconIndex(1 * 16 + 2)
				.setItemName("ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		CoreProxy.addName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new BuildCraftItem(Integer.parseInt(goldenGearId.value)))
				.setIconIndex(1 * 16 + 3)
				.setItemName("goldGearItem");
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
				Character.valueOf('G'), ironGearItem });
		CoreProxy.addName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new BuildCraftItem(Integer.parseInt(diamondGearId.value)))
				.setIconIndex(1 * 16 + 4)
				.setItemName("diamondGearItem");
		craftingmanager.addRecipe(new ItemStack(diamondGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.diamond,
				Character.valueOf('G'), goldGearItem });
		CoreProxy.addName(diamondGearItem, "Diamond Gear");

		BuildCraftCore.mainConfiguration.save();
	}
	
	
	public static void initializeModel (BaseMod mod) {
		 blockByEntityModel = ModLoader.getUniqueBlockModelID(mod, true);
		 pipeModel = ModLoader.getUniqueBlockModelID(mod, true);
		 markerModel = ModLoader.getUniqueBlockModelID(mod, false);
		 oilModel = ModLoader.getUniqueBlockModelID(mod, false);
	}
	
}
