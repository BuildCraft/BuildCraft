/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.silicon;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.DefaultProps;

public class BlockLaser extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private Icon textureTop, textureBottom, textureSide;

	public BlockLaser(int i) {
		super(i, Material.iron);
		setHardness(0.5F);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public int getRenderType() {
		return SiliconProxy.laserBlockModel;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileLaser();
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public Icon getBlockTextureFromSideAndMetadata(int i, int j) {
		if (i == ForgeDirection.values()[j].getOpposite().ordinal())
			return textureBottom;
		else if (i == j)
			return textureTop;
		else
			return textureSide;

	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);

		if (side <= 6) {
			meta = side;
		}

		return meta;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void func_94332_a(IconRegister par1IconRegister)
	{
	    textureTop = par1IconRegister.func_94245_a("buildcraft:laser_top");
        textureBottom = par1IconRegister.func_94245_a("buildcraft:laser_bottom");
        textureSide = par1IconRegister.func_94245_a("buildcraft:laser_side");
	}
}
