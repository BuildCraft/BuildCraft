/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.core.utils.Utils;

public abstract class BlockBuildCraft extends BlockContainer {

	protected static boolean keepInventory = false;
	protected final Random rand = new Random();

	protected BlockBuildCraft(Material material) {
		this(material, CreativeTabBuildCraft.BLOCKS);
	}

	protected BlockBuildCraft(Material material, CreativeTabBuildCraft creativeTab) {
		super(material);
		setCreativeTab(creativeTab.get());
		setHardness(5F);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IMachine && ((IMachine) tile).isActive()) {
			return super.getLightValue(world, x, y, z) + 8;
		} else {
			return super.getLightValue(world, x, y, z);
		}
	}
}
