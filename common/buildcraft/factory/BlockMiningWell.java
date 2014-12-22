/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockMiningWell extends BlockBuildCraft {

	//IIcon textureFront, textureSides, textureBack, textureTop;

	public BlockMiningWell() {
		super(Material.ground, new PropertyEnum[]{FACING_PROP});

		setHardness(5F);
		setResistance(10F);

		// TODO: set proper sound
		//setStepSound(soundStoneFootstep);
	}

	/*@Override
	public IIcon getIcon(int i, int j) {
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == 1) {
			return textureTop;
		} else if (i == 0) {
			return textureBack;
		} else if (i == j) {
			return textureFront;
		} else if (j >= 0 && j < 6 && EnumFacing.values()[j].getOpposite().ordinal() == i) {
			return textureBack;
		} else {
			return textureSides;
		}
	}*/

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entityliving, stack);
		EnumFacing orientation = Utils.get2dOrientation(entityliving);
		//TODO: Check if that is correct
		world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()), 1);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		removePipes(world, pos);
	}

	public void removePipes(World world, BlockPos pos) {
		for (int depth = pos.getY() - 1; depth > 0; depth--) {
			BlockPos t = new BlockPos(pos.getX(), depth, pos.getZ());
			Block pipe = world.getBlockState(t).getBlock();
			if (pipe != BuildCraftFactory.plainPipeBlock) {
				break;
			}
			world.setBlockToAir(t);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileMiningWell();
	}
}
