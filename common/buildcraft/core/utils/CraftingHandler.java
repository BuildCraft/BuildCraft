package buildcraft.core.utils;


import net.minecraft.item.Item;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;

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
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftEnergy.engineBlock))){
            		if (Event.crafting.getItemDamage() == 0) {
                		Event.player.addStat(BuildCraftCore.engineAchievement1, 1);
            		}
        		else if (Event.crafting.getItemDamage() == 1) {
                		Event.player.addStat(BuildCraftCore.engineAchievement2, 1);
            		}
            		else if (Event.crafting.getItemDamage() == 2) {
                		Event.player.addStat(BuildCraftCore.engineAchievement3, 1);
            		}
        	}
		if(Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftFactory.autoWorkbenchBlock))){
			Event.player.addStat(BuildCraftCore.aLotOfCraftingAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftFactory.miningWellBlock))){
			Event.player.addStat(BuildCraftCore.straightDownAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftFactory.quarryBlock))){
			Event.player.addStat(BuildCraftCore.chunkDestroyerAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftFactory.refineryBlock))){
			Event.player.addStat(BuildCraftCore.refineAndRedefineAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftBuilders.fillerBlock))){
			Event.player.addStat(BuildCraftCore.fasterFillingAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftSilicon.laserBlock))){
			Event.player.addStat(BuildCraftCore.tinglyLaserAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftSilicon.assemblyTableBlock))){
			Event.player.addStat(BuildCraftCore.timeForSomeLogicAchievement, 1);
		}
		if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftBuilders.architectBlock))){
            		Event.player.addStat(BuildCraftCore.architectAchievement, 1);
        	}
        	if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftBuilders.builderBlock))){
            		Event.player.addStat(BuildCraftCore.builderAchievement, 1);
        	}
        	if (Event.crafting.getItem().equals(BuildCraftBuilders.blueprintItem)){
            		Event.player.addStat(BuildCraftCore.blueprintAchievement, 1);
        	}
        	if (Event.crafting.getItem().equals(BuildCraftBuilders.templateItem)){
            		Event.player.addStat(BuildCraftCore.templateAchievement, 1);
        	}
        	if (Event.crafting.getItem().equals(Item.getItemFromBlock(BuildCraftBuilders.libraryBlock))){
            		Event.player.addStat(BuildCraftCore.libraryAchievement, 1);
        	}
	}

}
