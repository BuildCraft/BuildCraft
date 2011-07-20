package net.minecraft.src;

import java.io.File;
import java.util.TreeMap;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.BuildCraftConfiguration;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.PowerFramework;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.RedstonePowerFramework;
import net.minecraft.src.forge.Configuration.Property;
import net.minecraft.src.forge.Configuration.PropertyKind;

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
	
	public static PowerFramework powerFramework;
	
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
						PropertyKind.General, DefaultProps.CURRENT_CONTINUOUS);
		continuousCurrent.comment = "set to true for allowing machines to be driven by continuous current";

		continuousCurrentModel = Boolean.parseBoolean(continuousCurrent.value);
		
		Property powerFrameworkClass = BuildCraftCore.mainConfiguration
		.getOrCreateProperty("power.framework",
				PropertyKind.General, RedstonePowerFramework.class.getName());
		
		try {
			powerFramework = (PowerFramework) Class
					.forName(powerFrameworkClass.value).getConstructor(null)
					.newInstance(null);
		} catch (Throwable e) {
			e.printStackTrace();
			powerFramework = new RedstonePowerFramework();
		}
		
		mainConfiguration.save();
	}
	
	public static void initializeGears () {
		if (gearsInitialized) {
			return;
		}
		
		Property woodenGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("woodenGearItem.id", PropertyKind.Item,
						DefaultProps.WOODEN_GEAR_ID);
		Property stoneGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("stoneGearItem.id", PropertyKind.Item,
						DefaultProps.STONE_GEAR_ID);
		Property ironGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("ironGearItem.id", PropertyKind.Item,
						DefaultProps.IRON_GEAR_ID);
		Property goldenGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("goldenGearItem.id", PropertyKind.Item,
						DefaultProps.GOLDEN_GEAR_ID);
		Property diamondGearId = BuildCraftCore.mainConfiguration
				.getOrCreateIntProperty("diamondGearItem.id",
						PropertyKind.Item, DefaultProps.DIAMOND_GEAR_ID);		
		
		BuildCraftCore.mainConfiguration.save();
		
		gearsInitialized = true;
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		woodenGearItem = (new Item(Integer.parseInt(woodenGearId.value)))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/core/gui/wooden_gear.png"))
				.setItemName("woodenGearItem");
		craftingmanager.addRecipe(new ItemStack(woodenGearItem), new Object[] {
		" S ", "S S", " S ", Character.valueOf('S'), Item.stick});
		CoreProxy.addName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new Item(Integer.parseInt(stoneGearId.value)))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/core/gui/stone_gear.png"))
				.setItemName("stoneGearItem");
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
				Character.valueOf('G'), woodenGearItem });
		CoreProxy.addName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new Item(Integer.parseInt(ironGearId.value)))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/core/gui/iron_gear.png"))
				.setItemName("ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		CoreProxy.addName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new Item(Integer.parseInt(goldenGearId.value)))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/core/gui/golden_gear.png"))
				.setItemName("goldGearItem");
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
				Character.valueOf('G'), ironGearItem });
		CoreProxy.addName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new Item(Integer.parseInt(diamondGearId.value)))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/core/gui/diamond_gear.png"))
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
