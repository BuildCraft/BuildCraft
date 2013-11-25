/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockMiningWell extends BlockBuildCraft {

	Icon textureFront, textureSides, textureBack, textureTop;

	public BlockMiningWell(int i) {
		super(i, Material.ground);

		setHardness(5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
	}

	@Override
	public Icon getIcon(int i, int j) {
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
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(),1);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		super.breakBlock(world, x, y, z, id, meta);
		removePipes(world, x, y, z);
	}

	public void removePipes(World world, int x, int y, int z) {
		for (int depth = y - 1; depth > 0; depth--) {
			int pipeID = world.getBlockId(x, depth, z);
			if (pipeID != BuildCraftFactory.plainPipeBlock.blockID) {
				break;
			}
			world.setBlockToAir(x, depth, z);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileMiningWell();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
	    textureFront = par1IconRegister.registerIcon("buildcraft:miningwell_front");
        textureSides = par1IconRegister.registerIcon("buildcraft:miningwell_side");
        textureBack = par1IconRegister.registerIcon("buildcraft:miningwell_back");
        textureTop = par1IconRegister.registerIcon("buildcraft:miningwell_top");
	}
}
