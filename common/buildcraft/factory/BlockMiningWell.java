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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockMiningWell extends BlockBuildCraft {

	IIcon textureFront, textureSides, textureBack, textureTop;

	public BlockMiningWell() {
		super(Material.ground);

		setHardness(5F);
		setResistance(10F);

		// TODO: set proper sound
		//setStepSound(soundStoneFootstep);
	}

	@Override
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
		} else if (j >= 0 && j < 6 && ForgeDirection.values()[j].getOpposite().ordinal() == i) {
			return textureBack;
		} else {
			return textureSides;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		ForgeDirection orientation = Utils.get2dOrientation(entityliving);
		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		super.breakBlock(world, x, y, z, block, meta);
		removePipes(world, x, y, z);
	}

	public void removePipes(World world, int x, int y, int z) {
		for (int depth = y - 1; depth > 0; depth--) {
			Block pipe = world.getBlock(x, depth, z);
			if (pipe != BuildCraftFactory.plainPipeBlock) {
				break;
			}
			world.setBlockToAir(x, depth, z);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileMiningWell();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    textureFront = par1IconRegister.registerIcon("buildcraft:miningwell_front");
        textureSides = par1IconRegister.registerIcon("buildcraft:miningwell_side");
        textureBack = par1IconRegister.registerIcon("buildcraft:miningwell_back");
        textureTop = par1IconRegister.registerIcon("buildcraft:miningwell_top");
	}
}
