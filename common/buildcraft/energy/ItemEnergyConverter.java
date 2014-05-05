/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemEnergyConverter extends ItemBlock {
	public ItemEnergyConverter(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean adv) {
		super.addInformation(itemStack, player, list, adv);
		list.add(TileEnergyConverter.getLocalizedModeName(itemStack));
		list.add("");
		// This is a bit too big in the tooltip at this stage.
		// list.addAll(Arrays.asList(StringUtils.localize("tile.energyConverter.tooltip").split("\\|")));
	}
}
