/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.core.ItemBlockBuildCraft;

public class ItemLaserTable extends ItemBlockBuildCraft {

	public ItemLaserTable(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		switch (stack.getItemDamage()) {
			case 0:
				return "tile.assemblyTableBlock";
			case 1:
				return "tile.assemblyWorkbenchBlock";
			case 2:
				return "tile.integrationTableBlock";
            case 3:
                return "tile.chargingTableBlock";
		}
		return super.getUnlocalizedName();
	}

	@Override
	public int getMetadata(int meta) {
		return meta >= 0 && meta < EnumLaserTableType.values().length ? meta : 0;
	}
}
