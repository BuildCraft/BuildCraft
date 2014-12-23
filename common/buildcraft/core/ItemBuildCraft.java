/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.ModelHelper;

public class ItemBuildCraft extends Item implements IModelRegister {
	private boolean passSneakClick = false;

	public ItemBuildCraft() {
		this(CreativeTabBuildCraft.ITEMS);
	}

	public ItemBuildCraft(CreativeTabBuildCraft creativeTab) {
		super();

		setCreativeTab(creativeTab.get());
	}

	public Item setPassSneakClick(boolean passClick) {
		this.passSneakClick = passClick;
		return this;
	}

	@Override
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
		return passSneakClick;
	}

	public void registerModels() {
		ModelHelper.registerItemModel(this, 0, "");
	}
}
