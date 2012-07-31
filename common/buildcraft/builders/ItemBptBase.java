/** 
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import java.util.List;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.BptBase;
import buildcraft.core.ItemBuildCraft;

import net.minecraft.src.Entity;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public abstract class ItemBptBase extends ItemBuildCraft {

	public ItemBptBase(int i) {
		super(i);

		maxStackSize = 1;
		iconIndex = 5 * 16 + 0;
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public abstract int getIconFromDamage(int i);

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public void addInformation(ItemStack itemstack, List list) {
		BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(itemstack.getItemDamage());
		if (bpt != null) {
			list.add(bpt.getName());
		}
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {}

}
