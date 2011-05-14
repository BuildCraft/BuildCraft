package net.minecraft.src;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.buildcraft.core.Core;
import net.minecraft.src.buildcraft.core.ITickListener;
import net.minecraft.src.buildcraft.core.Utils;

public class BuildCraftCore {
	private static boolean initialized = false;
	
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;

	private static class TickContainer {
		ITickListener listener;
		int pace;
	}
	
	public static HashMap <ITickListener, TickContainer> tickListeners = new HashMap <ITickListener, TickContainer> ();
	
	public static LinkedList<TickContainer> tickListenersScheduledForAddition = new LinkedList<TickContainer>(); 
	
	public static LinkedList <ITickListener> tickListenersScheduledForRemoval = new LinkedList <ITickListener> (); 
	
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
		Core.addName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"stoneGearItem.id", "3801")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/stone_gear.png")).setItemName(
				"stoneGearItem");
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
				Character.valueOf('G'), woodenGearItem });
		Core.addName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"ironGearItem.id", "3802")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/iron_gear.png")).setItemName(
				"ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		Core.addName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"goldGearItem.id", "3803")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/golden_gear.png")).setItemName(
				"goldGearItem");
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
				Character.valueOf('G'), ironGearItem });
		Core.addName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"diamondGearItem.id", "3804")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/diamond_gear.png")).setItemName(
				"diamondGearItem");
		craftingmanager.addRecipe(new ItemStack(diamondGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.diamond,
				Character.valueOf('G'), goldGearItem });
		Core.addName(diamondGearItem, "Diamond Gear");
		
		Utils.saveProperties();
	}
	    
    long lastTick = 0;
    
    public static void registerTicksListener (ITickListener listener, int pace) {
    	//  TODO: move registers on tiles and use the date to select when to
    	//  make the call.
    	TickContainer container = new TickContainer();
    	container.listener = listener;
    	container.pace = pace;    	    	
    	tickListenersScheduledForAddition.add(container);
    }
    
    public void OnTickInGame()
    {    
    	for (ITickListener listener : tickListenersScheduledForRemoval) {    	    		
    		if (tickListeners.containsKey(listener)) {
    			tickListeners.remove(listener);
    		}
    	}
    	
    	for (TickContainer container : tickListenersScheduledForAddition) {    		    		    	
    		tickListeners.put (container.listener, container);    		
    	}
    	
    	tickListenersScheduledForAddition.clear ();
    	tickListenersScheduledForRemoval.clear ();
    	
    	if (Core.getWorld().getWorldTime() != lastTick) {    		    		
    		lastTick = Core.getWorld().getWorldTime();
    		
    		for (TickContainer container : tickListeners.values()) {
    			if (lastTick % container.pace == 0) {
    				container.listener.tick();	
    			}				
			}    		
    	}
    	
    }        

	public static void unregisterTicksListener(ITickListener tilePipe) {
		tickListenersScheduledForRemoval.add(tilePipe);
	}
}
