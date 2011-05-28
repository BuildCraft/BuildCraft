package net.minecraft.src;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;

public class BuildCraftCore {
	
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
	
	public static int customTextureModel;
	public static int blockByEntityModel;
	public static int pipeModel;
	
	public static void initialize () {
		if (initialized) {
			return;
		}
		
		initialized = true;
		Utils.loadProperties();
		
		redLaserTexture = 0 * 16 + 2;
		blueLaserTexture = 0 * 16 + 1;
		stripesLaserTexture = 0 * 16 + 3;
		transparentTexture = 0 * 16 + 0;

		
	}
	
	public static void initializeGears () {
		if (gearsInitialized) {
			return;
		}
		
		gearsInitialized = true;
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		woodenGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"woodenGearItem.id", "3800"))))
				.setIconIndex(
						ModLoader
								.addOverride("/gui/items.png",
										"/net/minecraft/src/buildcraft/core/gui/wooden_gear.png"))
				.setItemName("woodenGearItem");
		craftingmanager.addRecipe(new ItemStack(woodenGearItem), new Object[] {
		" S ", "S S", " S ", Character.valueOf('S'), Item.stick});
		CoreProxy.addName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"stoneGearItem.id", "3801")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/stone_gear.png")).setItemName(
				"stoneGearItem");
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
				Character.valueOf('G'), woodenGearItem });
		CoreProxy.addName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"ironGearItem.id", "3802")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/iron_gear.png")).setItemName(
				"ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		CoreProxy.addName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"goldGearItem.id", "3803")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/golden_gear.png")).setItemName(
				"goldGearItem");
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
				Character.valueOf('G'), ironGearItem });
		CoreProxy.addName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"diamondGearItem.id", "3804")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/diamond_gear.png")).setItemName(
				"diamondGearItem");
		craftingmanager.addRecipe(new ItemStack(diamondGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.diamond,
				Character.valueOf('G'), goldGearItem });
		CoreProxy.addName(diamondGearItem, "Diamond Gear");
		
		continuousCurrentModel = Boolean.parseBoolean(Utils.getProperty(
				"current.continous", "false"));

		Utils.saveProperties();
	}
	
	
	public static void initializeModel (BaseMod mod) {
		 blockByEntityModel = ModLoader.getUniqueBlockModelID(mod, true);
		 customTextureModel = ModLoader.getUniqueBlockModelID(mod, true);
		 pipeModel = ModLoader.getUniqueBlockModelID(mod, true);
	}
	
}
