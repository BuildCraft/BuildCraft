package buildcraft.core.utils;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import buildcraft.BuildCraftCore;

public class CraftingHandler {
	
	@SubscribeEvent
	public void onCrafing(PlayerEvent.ItemCraftedEvent Event){
		if (Event.crafting.getItem().equals(BuildCraftCore.woodenGearItem)){
			Event.player.addStat(BuildCraftCore.woodenGearAchievement, 1);
		}
		if (Event.crafting.getItem().equals(BuildCraftCore.stoneGearItem)){
			Event.player.addStat(BuildCraftCore.stoneGearAchievement, 1);
		}
		if (Event.crafting.getItem().equals(BuildCraftCore.ironGearItem)){
			Event.player.addStat(BuildCraftCore.ironGearAchievement, 1);
		}
		if (Event.crafting.getItem().equals(BuildCraftCore.goldGearItem)){
			Event.player.addStat(BuildCraftCore.goldGearAchievement, 1);
		}
		if (Event.crafting.getItem().equals(BuildCraftCore.diamondGearItem)){
			Event.player.addStat(BuildCraftCore.diamondGearAchievement, 1);
		}
		if (Event.crafting.getItem().equals(BuildCraftCore.wrenchItem)){
			Event.player.addStat(BuildCraftCore.wrenchAchievement, 1);
		}
	}

}
