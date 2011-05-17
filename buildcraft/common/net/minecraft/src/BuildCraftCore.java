package net.minecraft.src;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;

public class BuildCraftCore {
	private static boolean initialized = false;
	
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;

	public static void initialize () {
		if (initialized) {
			return;
		}
		
		initialized = true;
		Utils.loadProperties();
		
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
		
		Utils.saveProperties();
	}
}
