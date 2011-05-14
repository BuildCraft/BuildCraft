package net.minecraft.src;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.ITickListener;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.Utils;

public class mod_BuildCraftCore extends BaseMod {	

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
		ModLoader.AddName(woodenGearItem, "Wooden Gear");
		
		stoneGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"stoneGearItem.id", "3801")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/stone_gear.png")).setItemName(
				"stoneGearItem");
		craftingmanager.addRecipe(new ItemStack(stoneGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Block.cobblestone,
				Character.valueOf('G'), woodenGearItem });
		ModLoader.AddName(stoneGearItem, "Stone Gear");
		
		ironGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"ironGearItem.id", "3802")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/iron_gear.png")).setItemName(
				"ironGearItem");
		craftingmanager.addRecipe(new ItemStack(ironGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotIron,
				Character.valueOf('G'), stoneGearItem });
		ModLoader.AddName(ironGearItem, "Iron Gear");		
		
		goldGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"goldGearItem.id", "3803")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/golden_gear.png")).setItemName(
				"goldGearItem");
		craftingmanager.addRecipe(new ItemStack(goldGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.ingotGold,
				Character.valueOf('G'), ironGearItem });
		ModLoader.AddName(goldGearItem, "Gold Gear");
		
		diamondGearItem = (new Item(Integer.parseInt(Utils.getProperty(
				"diamondGearItem.id", "3804")))).setIconIndex(
				ModLoader.addOverride("/gui/items.png",
						"/net/minecraft/src/buildcraft/core/gui/diamond_gear.png")).setItemName(
				"diamondGearItem");
		craftingmanager.addRecipe(new ItemStack(diamondGearItem), new Object[] {
				" I ", "IGI", " I ", Character.valueOf('I'), Item.diamond,
				Character.valueOf('G'), goldGearItem });
		ModLoader.AddName(diamondGearItem, "Diamond Gear");
		
		Utils.saveProperties();
	}
	
	
	public mod_BuildCraftCore () {		
		mod_BuildCraftCore.initialize();				
		
		ModLoader.SetInGameHook(this, true, false);											
	}
	
	@Override
	public String Version() {
		return "1.5_01.4";
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
    
    public void OnTickInGame(Minecraft minecraft)
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
    	
    	if (minecraft.theWorld.getWorldTime() != lastTick) {    		    		
    		lastTick = minecraft.theWorld.getWorldTime();
    		
    		for (TickContainer container : tickListeners.values()) {
    			if (lastTick % container.pace == 0) {
    				container.listener.tick(minecraft);	
    			}				
			}    		
    	}
    	
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	map.put (EntityPassiveItem.class, new RenderItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());
    }

	public static void unregisterTicksListener(ITickListener tilePipe) {
		tickListenersScheduledForRemoval.add(tilePipe);
	}
}
