/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;

public class BlockMiningWell extends BlockMachineRoot {

	int textureFront, textureSides, textureBack, textureTop;

	public BlockMiningWell(int i) {
		super(i, Material.ground);

		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);

		textureFront = 16 * 2 + 3;
		textureSides = 16 * 2 + 5;
		textureBack = 16 * 2 + 6;
		textureTop = 16 * 2 + 4;

	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (j == 0 && i == 3)
			return textureFront;

		if (i == 1)
			return textureTop;
		else if (i == 0)
			return textureBack;
		else if (i == j)
			return textureFront;
		else if (j >= 0 && j < 6 && ForgeDirection.values()[j].getOpposite().ordinal() == i)
			return textureBack;
		else
			return textureSides;
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal());
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		super.breakBlock(world, x, y, z, id, meta);

		for (int depth = y - 1; depth > 0; depth--) {
			int pipeID = world.getBlockId(x, depth, z);
			if (pipeID != BuildCraftFactory.plainPipeBlock.blockID) {
				break;
			}
			world.setBlockWithNotify(x, depth, z, 0);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileMiningWell();
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
