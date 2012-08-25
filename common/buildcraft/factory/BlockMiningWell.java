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

import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.core.DefaultProps;
import buildcraft.core.Utils;
import buildcraft.factory.BlockMachineRoot;

import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;


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
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == 1) {
			return textureTop;
		} else if (i == 0) {
			return textureBack;
		} else if (i == j) {
			return textureFront;
		} else if (j >= 0 && j < 6 && Orientations.values()[j].reverse().ordinal() == i) {
			return textureBack;
		} else {
			return textureSides;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
		Orientations orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse().ordinal());
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
