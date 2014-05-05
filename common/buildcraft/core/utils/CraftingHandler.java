/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
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
	public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
		Item item = event.crafting.getItem();

		if (item.equals(BuildCraftCore.woodenGearItem)) {
			event.player.addStat(BuildCraftCore.woodenGearAchievement, 1);
		} else if (item.equals(BuildCraftCore.stoneGearItem)) {
			event.player.addStat(BuildCraftCore.stoneGearAchievement, 1);
		} else if (item.equals(BuildCraftCore.ironGearItem)) {
			event.player.addStat(BuildCraftCore.ironGearAchievement, 1);
		} else if (item.equals(BuildCraftCore.goldGearItem)) {
			event.player.addStat(BuildCraftCore.goldGearAchievement, 1);
		} else if (item.equals(BuildCraftCore.diamondGearItem)) {
			event.player.addStat(BuildCraftCore.diamondGearAchievement, 1);
		} else if (item.equals(BuildCraftCore.wrenchItem)) {
			event.player.addStat(BuildCraftCore.wrenchAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftEnergy.engineBlock))) {
			if (event.crafting.getItemDamage() == 0) {
				event.player.addStat(BuildCraftCore.engineAchievement1, 1);
			} else if (event.crafting.getItemDamage() == 1) {
				event.player.addStat(BuildCraftCore.engineAchievement2, 1);
			} else if (event.crafting.getItemDamage() == 2) {
				event.player.addStat(BuildCraftCore.engineAchievement3, 1);
			}
		} else if (item.equals(Item.getItemFromBlock(BuildCraftFactory.autoWorkbenchBlock))) {
			event.player.addStat(BuildCraftCore.aLotOfCraftingAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftFactory.miningWellBlock))) {
			event.player.addStat(BuildCraftCore.straightDownAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftFactory.quarryBlock))) {
			event.player.addStat(BuildCraftCore.chunkDestroyerAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftFactory.refineryBlock))) {
			event.player.addStat(BuildCraftCore.refineAndRedefineAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftBuilders.fillerBlock))) {
			event.player.addStat(BuildCraftCore.fasterFillingAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftSilicon.laserBlock))) {
			event.player.addStat(BuildCraftCore.tinglyLaserAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftSilicon.assemblyTableBlock))) {
			event.player.addStat(BuildCraftCore.timeForSomeLogicAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftBuilders.architectBlock))) {
			event.player.addStat(BuildCraftCore.architectAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftBuilders.builderBlock))) {
			event.player.addStat(BuildCraftCore.builderAchievement, 1);
		} else if (item.equals(BuildCraftBuilders.blueprintItem)) {
			event.player.addStat(BuildCraftCore.blueprintAchievement, 1);
		} else if (item.equals(BuildCraftBuilders.templateItem)) {
			event.player.addStat(BuildCraftCore.templateAchievement, 1);
		} else if (item.equals(Item.getItemFromBlock(BuildCraftBuilders.libraryBlock))) {
			event.player.addStat(BuildCraftCore.libraryAchievement, 1);
		}
	}

}
