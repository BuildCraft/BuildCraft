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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.tiles.IHasWork;
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
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entity, stack);
		FMLCommonHandler.instance().bus().post(new BlockPlacedDownEvent((EntityPlayer) entity, world.getBlockState(pos), pos));
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		super.breakBlock(world, pos, state);
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		if (hasTileEntity(world.getBlockState(pos))) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof IHasWork && ((IHasWork) tile).hasWork()) {
				return super.getLightValue(world, pos) + 8;
			}
		}
		return super.getLightValue(world, pos);
	}
}
