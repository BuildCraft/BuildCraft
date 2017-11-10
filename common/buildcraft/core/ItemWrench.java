/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLever;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.utils.BlockUtils;

public class ItemWrench extends ItemBuildCraft implements IToolWrench {
	private final Set<Class<? extends Block>> shiftRotations = new HashSet<Class<? extends Block>>();
	private final Set<Class<? extends Block>> blacklistedRotations = new HashSet<Class<? extends Block>>();

	public ItemWrench() {
		super();

		setFull3D();
		setMaxStackSize(1);
		shiftRotations.add(BlockLever.class);
		shiftRotations.add(BlockButton.class);
		shiftRotations.add(BlockChest.class);
		blacklistedRotations.add(BlockBed.class);
		setHarvestLevel("wrench", 0);
	}

	private boolean isClass(Set<Class<? extends Block>> set, Class<? extends Block> cls) {
		for (Class<? extends Block> shift : set) {
			if (shift.isAssignableFrom(cls)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		Block block = world.getBlock(x, y, z);

		if (block == null || isClass(blacklistedRotations, block.getClass())) {
			return false;
		}

		if (player.isSneaking() != isClass(shiftRotations, block.getClass())) {
			return false;
		}

		// Double chests should NOT be rotated.
		if (block instanceof BlockChest && BlockUtils.getOtherDoubleChest(world.getTileEntity(x, y, z)) != null) {
			return false;
		}

		if (block.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side))) {
			player.swingItem();
			return !world.isRemote;
		}
		return false;
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z) {
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
		player.swingItem();
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}
}
